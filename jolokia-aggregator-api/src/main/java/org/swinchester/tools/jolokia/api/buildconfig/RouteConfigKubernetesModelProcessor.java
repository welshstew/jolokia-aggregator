package org.swinchester.tools.jolokia.api.buildconfig;

import io.fabric8.openshift.api.model.TemplateBuilder;

/**
 * Created by swinchester on 24/06/16.
 */
public class RouteConfigKubernetesModelProcessor {

    public void on(TemplateBuilder builder) {
        builder.addNewRouteObject()
                .withNewMetadata()
                    .withName("jolokia-aggregator-api-route")
                .endMetadata()
                .withNewSpec()
                .withHost("${ROUTE_HOST_NAME}")
                .withNewTo()
                .withKind("Service")
                .withName("jolokia-aggregator-svc")
                .endTo()
                .endSpec()
                .endRouteObject()
                .build();
    }
}
