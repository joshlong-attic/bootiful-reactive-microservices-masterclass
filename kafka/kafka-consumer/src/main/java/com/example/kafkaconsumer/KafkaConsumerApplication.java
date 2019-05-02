package com.example.kafkaconsumer;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import reactor.core.publisher.Flux;

@EnableBinding(Sink.class)
@SpringBootApplication
@Log4j2
public class KafkaConsumerApplication {

	@StreamListener
	public void consumeNewGreetings(@Input(Sink.INPUT) Flux<String> greetings) {
		log.info("starting up!");
		greetings.subscribe(log::info);
	}

	public static void main(String[] args) {
		SpringApplication.run(KafkaConsumerApplication.class, args);
	}

}
