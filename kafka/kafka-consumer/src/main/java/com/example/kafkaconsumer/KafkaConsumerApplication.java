package com.example.kafkaconsumer;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import reactor.core.publisher.Flux;

import java.util.Map;

@Log4j2
@EnableBinding(Sink.class)
@SpringBootApplication
public class KafkaConsumerApplication {



	@StreamListener
	public void processNewMessages(@Input(Sink.INPUT) Flux<Map<String, String>> greetings) {
		greetings.subscribe(greeting -> log.info("new message: " + greeting));
	}

	public static void main(String[] args) {
		SpringApplication.run(KafkaConsumerApplication.class, args);
	}

}
