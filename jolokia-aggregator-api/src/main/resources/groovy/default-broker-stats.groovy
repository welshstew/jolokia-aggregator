
/**
 * Created by swinchester on 8/07/16.
 */
def attribute = [
        "TotalConnectionsCount",
        "TotalConsumerCount",
        "TotalDequeueCount",
        "TotalEnqueueCount",
        "TotalMessageCount",
        "TotalProducerCount",
        "StoreLimit",
        "StorePercentUsage",
        "TempLimit",
        "TempPercentUsage",
        "MaxMessageSize",
        "MemoryLimit",
        "MemoryPercentUsage",
        "MinMessageSize",
        "AverageMessageSize",
        "BrokerId",
        "BrokerName",
        "BrokerVersion",
        "CurrentConnectionsCount"
]
def body = [mbean:"org.apache.activemq:type=Broker,brokerName=kube-lookup",
            type:"read",
            attribute: attribute]

request.body = body




