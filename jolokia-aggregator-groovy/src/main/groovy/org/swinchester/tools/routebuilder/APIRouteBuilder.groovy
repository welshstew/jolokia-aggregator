package org.swinchester.tools.routebuilder

import groovy.json.JsonOutput
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.apache.camel.builder.PredicateBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.processor.validation.PredicateValidationException
import org.springframework.beans.factory.annotation.Value
import org.swinchester.tools.jolokia.http.JolokiaRequestProcessor

/**
 * Created by swinchester on 5/07/16.
 */
class APIRouteBuilder extends RouteBuilder {

    @Value('#{systemEnvironment.OPENSHIFT_MASTER_URI}')
    private String masterURI

    String getMasterURI() {
        return masterURI
    }

    void setMasterURI(String masterURI) {
        this.masterURI = masterURI
    }

    @Override
    void configure() throws Exception {

        rest("/jolokia")
                .get("/ping").to("direct:ping")
                .post("/aggregate").consumes("application/json").to("direct:proxyAggregate")
                .post("/proxy").consumes("application/json").to("direct:proxy")
                .get("/brokerstats").consumes("application/json").to("direct:brokerstats")
                .post("/purge").consumes("application/json").to("direct:purgeQueue") //expect a queue name


        onException(PredicateValidationException.class)
                .handled(true)
                .to("log:exceptions?level=ERROR")
                .process(new Processor() {
                    @Override
                    void process(Exchange exchange) throws Exception {
                        exchange.in.body = JsonOutput.toJson([error:'sorry - invalid request or params'])
                    }
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))

        onException(Exception.class)
                .handled(true)
                .to("log:exceptions?level=ERROR")
                .process(new Processor() {
            @Override
            void process(Exchange exchange) throws Exception {
                def outputMap = [:]
                outputMap.put('exception',exchange.properties['CamelExceptionCaught'])
                exchange.in.setBody(JsonOutput.toJson(outputMap))
                exchange.in.setHeader(Exchange.HTTP_RESPONSE_CODE, 400)
            }
        })

        from('direct:proxyAggregate')
            .enrich('direct:getPodNames')
            .setProperty('originalBody', simple('${body}'))
            .split(header('podNames'), new JsonAggregationStrategy()).parallelProcessing()
                .process(new JolokiaRequestProcessor(masterURI))
            .end()
            .to("log:stuff?showAll=true")



        Predicate queueExists = PredicateBuilder.isNotNull(header("queueName"))

        from('direct:purgeQueue')
                .validate(queueExists)
                .enrich('direct:getPodNames')
                .split(header('podNames'), new JsonAggregationStrategy()).parallelProcessing()
                    .process(new Processor() {
                        @Override
                        void process(Exchange exchange) throws Exception {
                            def jsonMapBody =  [type:'EXEC',
                                                mbean: "org.apache.activemq:type=Broker,brokerName=${exchange.in.body},destinationType=Queue,destinationName=${exchange.in.headers.'queueName'}",
                                                operation:'purge',
                                                ]
                            exchange.properties.put('originalBody', jsonMapBody)
                        }
                    })
                    .process(new JolokiaRequestProcessor(masterURI))
                .end()
                .to("log:stuff?showAll=true")


        from('direct:brokerstats')
            .enrich('direct:getPodNames')
            .setBody().groovy("resource:classpath:groovy/default-broker-stats.groovy")
            .setProperty('originalBody', simple('${body}'))
            .split(header('podNames'), new JsonAggregationStrategy()).parallelProcessing()
                .process(new JolokiaRequestProcessor(masterURI))
            .end()
            .to("log:stuff?showAll=true")

        from("direct:getPodNames")
            .processRef("kubeProcessor")

//        from("direct:aggregate")
//                .setHeader("queue", constant("queue.sample"))
//                .processRef("kubeProcessor")
//                .split(header("podNames"), new JsonAggregationStrategy())
//                    .processRef("jolokiaProcessor")
//                .end()
//                .to("log:stuff?showAll=true")


//                {
//                    "type" : "read",
//                    "mbean" : "org.apache.activemq:type=Broker,brokerName=broker-amq-1-t75cg,destinationType=Topic",
//                    "attribute" : ["QueueSize","ConsumerCount"]
//                }

        /**
         * destinationType = (*|Queue|Topic)
         * destinationName = (*|any characters)
         * brokerName = kube-lookup
         * attribute = List<String>
         */

//        from("timer:hello?period=100&repeatCount=1")
//                .process(new Processor() {
//            @Override
//            void process(Exchange exchange) throws Exception {
//                exchange.in.body = [type:"read",
//                                    mbean: "org.apache.activemq:type=Broker,brokerName=kube-lookup,destinationType=Queue,destinationName=queue.sample",
//                                    attribute: ["QueueSize", "ConsumerCount"]]
//            }
//        }).to("direct:proxyAggregate")

        from("direct:ping").setBody(constant([ping:'hello']))

        from("direct:proxy").to("log:hello")



    }
}


