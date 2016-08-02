# Kube Jolokia Aggregator API

Simple Camel/Groovy API which will call multiple jolokia's and aggregate the result.

[See Here - Jolokia API](https://jolokia.org/reference/html/protocol.html)

## How to Call it...

Currently there is a simple proxy...  Perhaps create some easy calls which wrap up the jolokia API to be less verbose...


####HTTP Headers:

- Authorization: Bearer hUfIhK7eirqEv3ZgrhizznRzAbdKd4rPxEqF51h9PvA  (oc whoami -t) - account must have access to do things.  Passthrough.
- kube-label: type=amq        - the labels to search kubernetes for mutiple amq pods
- kube-namespace: jolokia     - the namespace in which to search for the above labels

####HTTP Body:

Ad per the Jolokia API...  Just using the POST style at present.

```
{
"type" : "read",
"mbean" : "org.apache.activemq:type=Broker,brokerName=kube-lookup,destinationType=*,destinationName=*",
"attribute" : "QueueSize"
}
```

## Try it out:

- get a broker or 2 running on openshift.
- [http://localhost:9090/jolokia/brokerstats](http://localhost:9090/jolokia/brokerstats) - this aggregates 2 broker stats
- headers: Authorization, kube-label, and kube-namespace

## Getting this stuff Running:

```
#create imagestreams for amq and java fis stuff 
oc create -f https://raw.githubusercontent.com/jboss-fuse/application-templates/master/fis-image-streams.json -n openshift
oc create -f https://raw.githubusercontent.com/jboss-openshift/application-templates/master/amq/amq62-basic.json -n openshift

#create a new namespace and create a couple of brokers
oc new-project playground
oc new-app --template=amq62-basic --param=APPLICATION_NAME=broker1,MQ_USERNAME=admin,MQ_PASSWORD=admin,MQ_QUEUES=queue.one
oc new-app --template=amq62-basic --param=APPLICATION_NAME=broker2,MQ_USERNAME=admin,MQ_PASSWORD=admin,MQ_QUEUES=queue.one

#Create the template for this app
oc create -f https://raw.githubusercontent.com/welshstew/jolokia-aggregator/master/jolokia-aggregator-api/kube/kubernetes.yml

#get your local registry ip
oc get svc/docker-registry -n default | awk 'FNR >1 {print $2}'

#edit the template for this app (ensure the registry svc IP is correct)
oc edit templates/jolokia-aggregator-api

#create the jolokia aggregator
oc new-app --template=jolokia-aggregator-api --param=REGISTRY=172.30.252.41:5000,IS_PULL_NAMESPACE=playground

#give the API the permissions to read the kubernetes API
oc policy add-role-to-user view system:serviceaccount:playground:default -n playground

```

# NEEDS FIXING

- SVC/Route does not hook up correctly - metadata needs to be fixed...

## TODO

- Just call jolokia on an endpoint (like with a running amq)
- Discover pods with a particular label (like say thing=amq)
- for each pod, call jolokia with that pod in the url and bring back the queues


- message counts
- consumer counts
- actions - purging messages
- actions - 
- total broker statistics
-- total pending messages
-- total connection counts
-- total etc...