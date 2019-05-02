# A Bootiful Reactive Microservices Masterclass 

Useful to get everything built: `find . -iname pom.xml | xargs -I pom mvn -DskipTests=true -f pom install`


* Motivations for Microservices (x)
	*  Learning Organizations 
	*  Agility 

* Spring Boot "Bootcamp" (x)
	*  Starters
	*  Autoconfiguration

* Introduction to the Reactive Streams & Reactor (x)
	*  Cold & Hot Streams 
	*  Schedulers
	*  the Reactor Context

* Reactive Data Access (x)
	*  Spring Data MongoDB 
	*  R2DBC 

* The Web Tier (x)
	* Spring Webflux-powered HTTP APIs 
	* Websockets
	* the `WebClient` Reactive HTTP Client 
	* Actuator 

* Testing (x)
	*  Testing Reactive Pipelines 
	*  Testing Data Access 
	*  Testing Web Endpoints 
	*  Testing Microservices 

* Reliability Patterns (x)
	*  The new Spring Cloud Circuit Breaker 
	*  `onErrorResume(...)` 
	*  `retry(...)` 
	*  `on*`

* Edge Services (x)
	*  API Gateway
	*  API Adapters

* RSocket (x)
	*  Raw RSocket
	* Spring Message Mapping 

* Cloud Foundry (x)
	*  `cf push`
	*  The Reactive Java Client 
	
In order to use MongoDB you'll need a program like this: 
```
data=$HOME/Desktop/mongodb-data
mkdir -p $data
mongod --replSet my-replica-set --dbpath $data & 
mongo --eval "rs.initiate()"
```
