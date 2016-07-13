# Kube Jokokia Aggregator API

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