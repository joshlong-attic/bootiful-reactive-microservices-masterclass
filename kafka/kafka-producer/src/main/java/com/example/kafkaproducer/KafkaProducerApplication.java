package com.example.kafkaproducer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;


@EnableBinding(Source.class)
@SpringBootApplication
@RequiredArgsConstructor
@RestController
public class KafkaProducerApplication {

	private final Source source;

	@GetMapping("/greet/{name}")
	public void produce(@PathVariable String name) {
		MessageChannel output = this.source.output();
		Message<String> build = MessageBuilder.withPayload("Hello " + name + " @ " + Instant.now())
			.build();
		output.send(build);
	}

	public static void main(String[] args) {
		SpringApplication.run(KafkaProducerApplication.class, args);
	}
}
