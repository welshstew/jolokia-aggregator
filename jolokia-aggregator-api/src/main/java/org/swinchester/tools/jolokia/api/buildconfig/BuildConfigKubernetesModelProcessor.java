package org.swinchester.tools.jolokia.api.buildconfig;

import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.openshift.api.model.BuildTriggerPolicy;
import io.fabric8.openshift.api.model.ImageChangeTrigger;
import io.fabric8.openshift.api.model.TemplateBuilder;

import java.util.HashMap;
import java.util.Map;

public class BuildConfigKubernetesModelProcessor {

//As Taken from /usr/local/s2i/assemble:
//    # Use main class if given
//    local hawtapp_arg=""
//    if [ x"${JAVA_MAIN_CLASS}" != x ]; then
//            hawtapp_arg="-Dhawt-app.javaMainClass=${JAVA_MAIN_CLASS} io.fabric8:hawt-app-maven-plugin:${HAWTAPP_VERSION}:build"
//    fi

//    so we need to set HAWTAPP_VERSION - which should be the same as the fabric8 version


//    public void on(BuildConfigBuilder builder){
//        builder.withSpec(builder.getSpec())
//                .withNewMetadata()
//                .withName(ConfigParameters.APP_NAME + "-bc")
//                .withLabels(getLabels())
//                .endMetadata()
//                .withNewSpec()
//                .withTriggers(getTriggers())
//                .withNewSource()
//                .withNewGit()
//                .withUri("${GIT_URI}")
//                .endGit()
//                .withType("Git")
//                .endSource()
//                .withNewStrategy()
//                .withNewSourceStrategy()
//                .addNewEnv()
//                .withName("JAVA_MAIN_CLASS")
//                .withValue("org.apache.camel.spring.Main")
//                .endEnv()
//                .addNewEnv()
//                .withName("ARTIFACT_DIR")
//                .withValue("jolokia-aggregator-api")
//                .endEnv()
//                .addNewEnv()
//                .withName("HAWTAPP_VERSION")
//                .withValue("2.2.0.redhat-079")
//                .endEnv()
//                .withNewFrom()
//                .withKind("ImageStreamTag")
//                .withName("fis-java-openshift:1.0")
//                .withNamespace("openshift")
//                .endFrom()
//                .endSourceStrategy()
//                .withType("Source")
//                .endStrategy()
//                .withNewOutput()
//                .withNewTo()
//                .withKind("ImageStreamTag")
//                .withName(ConfigParameters.APP_NAME + ":${IS_TAG}")
//                .endTo()
//                .endOutput()
//                .endSpec().build();
//    }

    public void on(TemplateBuilder builder) {
        builder.addNewBuildConfigObject()
                .withNewMetadata()
                    .withName(ConfigParameters.APP_NAME + "-bc")
                    .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                    .withTriggers(getTriggers())
                    .withNewSource()
                        .withNewGit()
                            .withUri("${GIT_URI}")
                        .endGit()
                        .withType("Git")
                    .endSource()
                    .withNewStrategy()
                        .withNewSourceStrategy()
                            .addNewEnv()
                                .withName("JAVA_MAIN_CLASS")
                                .withValue("org.apache.camel.spring.Main")
                            .endEnv()
                            .addNewEnv()
                                .withName("ARTIFACT_DIR")
                                .withValue("jolokia-aggregator-api/target")
                            .endEnv()
                            .addNewEnv()
                                .withName("HAWTAPP_VERSION")
                                .withValue("2.2.0.redhat-079")
                            .endEnv()
                            .withNewFrom()
                                .withKind("ImageStreamTag")
                                .withName("fis-java-openshift:1.0")
                                .withNamespace("openshift")
                            .endFrom()
                        .endSourceStrategy()
                        .withType("Source")
                    .endStrategy()
                    .withNewOutput()
                        .withNewTo()
                            .withKind("ImageStreamTag")
                            .withName(ConfigParameters.APP_NAME + ":${IS_TAG}")
                        .endTo()
                    .endOutput()
                .endSpec()
            .endBuildConfigObject()
            .build();
    }

    private BuildTriggerPolicy getTriggers() {
        ObjectReference from = new ObjectReference();
        from.setName("fis-java-openshift:1.0");
        from.setKind("ImageStreamTag");
        from.setNamespace("openshift");

        ImageChangeTrigger imageChangeTrigger = new ImageChangeTrigger();
        imageChangeTrigger.setFrom(from);

        BuildTriggerPolicy policy = new BuildTriggerPolicy();
        policy.setType("ImageChange");
        policy.setImageChange(imageChangeTrigger);

        return policy;
    }

    private Map<String, String> getLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", ConfigParameters.APP_NAME);
        labels.put("project", ConfigParameters.APP_NAME);
        labels.put("version", "1.0.0-SNAPSHOT");
        labels.put("group", ConfigParameters.GROUP_NAME);

        return labels;
    }

}
