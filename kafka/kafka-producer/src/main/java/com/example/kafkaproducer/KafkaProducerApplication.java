package com.example.kafkaproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@SpringBootApplication
@EnableBinding(Source.class)
public class KafkaProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaProducerApplication.class, args);
	}
}

@RestController
class ProducerRestController {

	private final MessageChannel channel;

	ProducerRestController(Source producerBinding) {
		this.channel = producerBinding.output();
	}

	@PostMapping("/publish/{name}")
	Mono<Boolean> publish(@PathVariable String name) {
		var greetingMsg = MessageBuilder.withPayload(Collections.singletonMap("greeting", "Hello " + name + "!")).build();
		boolean send = this.channel.send(greetingMsg);
		return Mono.just(send);
	}
}

