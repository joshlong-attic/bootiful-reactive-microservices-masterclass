# A Bootiful Reactive Microservices Workshop 

Useful to get everything built: `find . -iname pom.xml | xargs -I pom mvn -DskipTests=true -f pom install`


* Motivations for Microservices 
	*  Learning Organizations 
	*  Agility 

* Spring Boot "Bootcamp" 
	*  Starters
	*  Autoconfiguration

* Introduction to the Reactive Streams & Reactor 
	*  Cold & Hot Streams 
	*  Schedulers
	*  the Reactor Context
	*  Adapting non-reactive event sources to reactive APIs with a reactive adapter

* Reactive Data Access 
	*  Spring Data MongoDB 
	*  R2DBC 

* The Web Tier 
	* Spring Webflux-powered HTTP APIs 
	* Websockets
	* the `WebClient` Reactive HTTP Client 
	* Actuator 
	* Spring Security 

* Testing
	*  Testing Reactive Pipelines 
	*  Testing Data Access 
	*  Testing Web Endpoints 
	*  Testing Microservices 

* Bootiful Configuration
	* basics 
	* Spring Cloud Config Server
	* Hashicorp Vault integration

* Reliability Patterns 
	*  The new Spring Cloud Circuit Breaker 
	*  `onErrorResume(...)` 
	*  `retry(...)` 
	*  `on*`
	* hedging 

* Edge Services 
	*  API Gateway
	*  API Adapters

* RSocket 
	* Raw RSocket
	* Spring Message Mapping 
	* `rsc`
	* Spring Integration RSocket
	* Bidirectional communication 
	* Spring Security 

* Kotlin
	* Ecosystem
	* Basics
	* Advanced

* The Cloud
	* Azure Spring Cloud
	*  `cf push` and Cloud Foundry's the Reactive Java Client 
	* Docker images in Spring Boot: `mvn spring-boot:build-image`
	
In order to use MongoDB you'll need a program like this: 
```
data=$HOME/Desktop/mongodb-data
mkdir -p $data
mongod --replSet my-replica-set --dbpath $data & 
mongo --eval "rs.initiate()"
```
