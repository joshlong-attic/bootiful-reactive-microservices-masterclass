package com.example.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
public class ClientApplication {


	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	ReactiveCircuitBreakerFactory circuitBreakerFactory() {
		var factory = new ReactiveResilience4JCircuitBreakerFactory();
		factory
			.configureDefault(s -> new Resilience4JConfigBuilder(s)
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build())
				.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
				.build());
		return factory;
	}

	@Bean
	@LoadBalanced
	WebClient.Builder myWebClient() {
		return WebClient.builder();
	}
}


@Log4j2
@RestController
@RequiredArgsConstructor
class HedgingDemo {

	private final WebClient client = WebClient.builder().build();
	private final DiscoveryClient discoveryClient;
	private final String greetingsServiceId = "GREETINGS-SERVICE".toLowerCase();


	@GetMapping("/edge/greetings/{name}")
	Publisher<Greeting> greet(@PathVariable String name) {
		var countOfNodes = 2;
		var instances = this.discoveryClient.getInstances(this.greetingsServiceId);
		Assert.isTrue(instances.size() >= countOfNodes, "there must be 2 or more instances");
//		var list = new HashSet<Flux<Greeting>>();
		var map = new HashMap<String, Flux<Greeting>>();
		while (map.size() < countOfNodes) {
			for (ServiceInstance si : instances) {
				if (Math.random() > .5 && map.size() < countOfNodes) {
					var uri = "http://" + si.getHost() + ':' + si.getPort();
					var result = greet(uri, name);
					map.put(uri, result);
				}
			}
		}
		log.info("the size of the list is " + map.size());
		return Flux.first(map.values());

	}

	Flux<Greeting> greet(String host, String name) {
		return this
			.client
			.get()
			.uri(host + "/greetings/{name}", name).retrieve()
			.bodyToFlux(Greeting.class);
	}

}

@Component
@Log4j2
class LoadbalancedDemo {

	private final WebClient.Builder builder;
	private final String greetingsServiceId = "GREETINGS-SERVICE".toLowerCase();
	private final ReactiveCircuitBreaker greetings;

	LoadbalancedDemo(WebClient.Builder builder, ReactiveCircuitBreakerFactory circuitBreakerFactory) {
		this.builder = builder;
		this.greetings = circuitBreakerFactory.create("greetings");
	}

	@EventListener
	public void refresh(ContextRefreshedEvent event) {
		log.info("refresh: " + event.getSource());
	}

	@EventListener
	public void heartbeat(HeartbeatEvent event) {
		log.info("heartbeat: " + event.toString());
	}

	@EventListener
	public void instanceRegisteredEvent(InstanceRegisteredEvent event) {
		log.info(event.getSource());
		log.info(event.getConfig());
	}

	@EventListener
	public void instancePreRegisteredEvent(InstancePreRegisteredEvent event) {
		log.info(event.getSource());
	}

	@EventListener(ApplicationReadyEvent.class)
	public void pingService() {


		Flux<Greeting> jane = this.builder
			.build()
			.get()
			.uri("http://" + this.greetingsServiceId + "/greetings/{name}", "Jane")
			.retrieve()
			.bodyToFlux(Greeting.class);

		Flux<Greeting> run = this.greetings.run(jane, throwable -> Flux.empty());
		run.subscribe(log::info);

		/*
		.retryBackoff(10, Duration.ofSeconds(1))
			.onErrorResume( ex -> Mono.empty())*/
		;
	}
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String name;
}


@Component
@Log4j2
class DiscoveryClientDemo {

	private final DiscoveryClient discoveryClient;
//	private final String appId;

	DiscoveryClientDemo(DiscoveryClient discoveryClient
		/*, @Value("${my-service-id}") String appId*/) {
		this.discoveryClient = discoveryClient;
//		this.appId = appId;
	}


	private String greetingsServiceId = "GREETINGS-SERVICE".toLowerCase();

	@EventListener(ApplicationReadyEvent.class)
	public void enumerateUsingDiscoveryClient() {

		List<ServiceInstance> instances = this.discoveryClient.getInstances(greetingsServiceId);
		this.discoveryClient.getServices().forEach(log::info);
		instances.forEach(serviceInstance -> log.info(serviceInstance.getHost() + ':' + serviceInstance.getPort()
			+ " ( " + serviceInstance.getInstanceId() + "/" + serviceInstance.getServiceId() + ")"));
		instances.forEach(si -> {
			log.info("size: " + si.getMetadata().keySet().size());
			si.getMetadata().forEach((p, k) -> log.info(p + '=' + k));
		});
	}

}

