package org.swinchester.tools.jolokia.http

import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by swinchester on 6/07/16.
 */
class KubeDiscoveryProcessor implements Processor{

    DefaultKubernetesClient client;

    Logger log = LoggerFactory.getLogger(this.class)

    @Override
    void process(Exchange exchange) throws Exception {

        String namespace = exchange.in.getHeader("kube-namespace") ?: "jolokia"
        String labelFilter = exchange.in.getHeader("kube-label") ?: "type=amq"

        def labelList = labelFilter.split("=")
        String labelName = labelList[0]
        String labelValue = labelList[1]

        def serverUrl = exchange.in.headers.'RequestHost' ?:"https://kubernetes.default"
        log.debug("about to call kubernetes to look for ${labelFilter} in ${namespace}")
        client = new DefaultKubernetesClient(serverUrl)
        PodList kubePods = client.inNamespace(namespace).pods().withLabel(labelName,labelValue).list()
        log.debug("called kubernetes and got a podlist")
        def podNames = kubePods.items.collect { it.metadata.name }
        log.debug("pod names matching label (${labelName}=${labelValue}) are : ${podNames}")
        exchange.in.headers.put("podNames", podNames)

    }
}
