# prices-quarkus-kafka-example-2
Kafka example with quarkus and kafka (amq streams). This is based on the Quarkus Kafka example [here](https://quarkus.io/guides/kafka). The difference is that the application is decomposed into two microservices and also modified to add more kafka topics and multiple methods of producing a kafka message. It also gives the user the ability to update the conversion rate and generate a new price on-demand. The code demonstrates two different patterns (Kafka Producer client and Smallrye Reactive Kafka Messaging) to write to Kafka Topics.

*This project uses JDK 8 but probably needs to be updated to use 11 since 8 is deprecated with later versions of Quarkus*

**TODO: Update Diagram**

![](images/kafka-guide-architecture.png)
![](images/kafka-guide-architecture-2.png)

## Local development

### Deploy kafka locally using docker-compose
```
docker-compose -f kafka/docker-compose.yaml up -d 
#to view the docker services
docker-compose -f kafka/docker-compose.yaml ps
# this should return two entries
```

To stop the running kafka
```sh
docker-compose -f kafka/docker-compose.yaml down
```

### Run the Price Generator
live-coding
```sh
cd price-generator
mvn clean compile quarkus:dev
```

### Run the Price Converter
live-coding
```sh
cd price-converter
mvn clean compile quarkus:dev
```

**Note**: If running both microservices concurrently, you may want to pass *-Ddebug=false* to avoid collision on debug ports

### Test
navigate to http://localhost:9083/prices.html to view the current conversion rate, latest updated conversion rate, and latest price

To generate a new conversion rate navigate http://localhost:9082/set-conversion-rate

To generate a new price navigate http://localhost:9082/set-price

Since the microservices in https://github.com/gbengataylor/prices-quarkus-kafka-example use different kakfa consumer groups, you can also boot those servers to observe
* the generated price
* the different in last price as the conversion rate in v2 microservice is updated


Every 5 seconds you should see the page refresh with a new price

**Note:** if you choose to deploy Kafka elsewhere or maybe you are developing in CodeReady Workspaces (CRW), you can pass the appropriate location of the kafka bootstrap to the local deployment using
```sh
-Dmp.messaging.outgoing.generated-price.bootstrap.servers=<bootstrap location>
```
For example
```sh
#kakfa_bootstrap=prices-cluster-kafka-bootstrap-prices-kafka.apps.cluster-60c1.60c1.example.opentlc.com:80
kakfa_bootstrap=prices-cluster-kafka-bootstrap.prices-kafka:9092 
mvn clean compile quarkus:dev -Dmp.messaging.outgoing.generated-price.bootstrap.servers=$kakfa_bootstrap
```


### Clean up local development
Remember to shutdown the kafka services once down with local development
```sh
docker-compose -f kafka/docker-compose.yaml down
```

## Deploy AMQ Streams
Set the OpenShift name
```sh
PRICES_PROJECT=prices-kafka
```
**Note**: The following commands assume that you are logged into and OpenShift cluster and that the AMQ Streams Operator has been installed on the cluster and is watching the namespace

```sh
oc new-project $PRICES_PROJECT
```

### Create the Kafka Cluster
You can cut and paste the contents of kafka/PriceKafkaCluster.yaml using the OpenShift Operator UI or just run the folllowiing
```sh
oc apply -f kafka/PriceKafkaCluster.yaml -n $PRICES_PROJECT
oc get Kafka
```
Wait till the Kafka cluster and zookeeper statefulsets, and prices-cluster-entity-operator deployment are running

### Create the Topic
You can cut and paste the contents of kafka/topic files using the OpenShift Operator UI or just run the folllowiing
```sh
oc apply -f kafka/topics/ -n $PRICES_PROJECT
oc get KakfaTopic
```

### if you want, you can test a topic using oc client
```sh
oc run kafka-producer -ti --image=registry.redhat.io/amq7/amq-streams-kafka-23:1.3.0 --rm=true --restart=Never -- bin/kafka-console-producer.sh --broker-list prices-cluster-kafka-bootstrap:9092 --topic prices

oc run kafka-consumer -ti --image=registry.redhat.io/amq7/amq-streams-kafka-23:1.3.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server prices-cluster-kafka-bootstrap:9092 --topic prices --from-beginning

# clean up after testing
oc delete pod kafka-producer
oc delete pod kafka-consumer
```
## Deploy microservices on OpenShift from remote git repo

```
oc new-app registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift~https://github.com/gbengataylor/prices-quarkus-kafka-example-2.git --context-dir=price-generator --name=price-generator-v2

oc expose svc price-generator-v2 

oc new-app registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift~https://github.com/gbengataylor/prices-quarkus-kafka-example-2.git --context-dir=price-converter --name=price-converter-v2

oc expose svc price-converter-v2 

oc label dc/price-generator-v2  app.kubernetes.io/part-of=prices-v2 --overwrite
oc label dc/price-generator-v2  app.openshift.io/runtime=java --overwrite 
oc label dc/price-converter-v2  app.kubernetes.io/part-of=prices-v2 --overwrite
oc label dc/price-converter-v2 app.openshift.io/runtime=java --overwrite 
```

## Deploy microservices on openshift using local git repo/source code

**Note**: This assumes that the Kafka Cluster has been deployed on OpenShift and in the same project
```sh
oc project $PRICES_PROJECT
```
Quarkus has an extension to faciliate deployment to OpenShift. 
```
./mvnw quarkus:add-extension -Dextensions="openshift"
```
The two microserivces already include the openshift extension so there is no need to run that command

S2I is also an option, but not used in this example

**Note**: If you want to build the image without deploying, the following command can be run (it performs an S2I build). This will create a BuildConfig, build the image, and push to an ImageStream
```sh
./mvnw clean package -Dquarkus.container-image.build=true
```

### Deploy Price Generator on OpenShift
```sh
cd price-generator
./mvnw clean package -Dquarkus.kubernetes.deploy=true
oc label dc/price-generator-v2  app.kubernetes.io/part-of=prices-v2 --overwrite
oc label dc/price-generator-v2 app.openshift.io/runtime=java --overwrite 
```

**Note**: if the Image gets built but the deployment fails (and may with JDK8), you can deploy the microservice with
```sh
oc new-app price-generator-v2:1.0-SNAPSHOT
oc expose service price-generator-v2
```

### Deploy Price Converter on OpenShift
```sh
cd price-converter
./mvnw clean package -Dquarkus.kubernetes.deploy=true
oc label dc/price-converter-v2  app.kubernetes.io/part-of=prices-v2 --overwrite
oc label dc/price-converter-v2 app.openshift.io/runtime=java --overwrite 
```
**Note**: if the Image gets built but the deployment fails(and may with JDK8), you can deploy the microservice with
```sh
oc new-app price-converter-v2:1.0-SNAPSHOT
oc expose service price-converter-v2
```

## Test On OpenShift
```
export URL="http://$(oc get route price-converter-v2 -o jsonpath='{.spec.host}')"
echo "Navigate to  URL: $URL/prices.html to view updated prices"

export URL="http://$(oc get route price-generator-v2 -o jsonpath='{.spec.host}')"
echo "Navigate to  URL: $URL/index.html to view links to update the conversion rate and prices"
```
