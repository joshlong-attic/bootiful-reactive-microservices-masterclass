package com.example;

import com.example.producer.GreetingRequest;
import com.example.producer.GreetingResponse;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

public class GreetingService {

	public Flux<GreetingResponse> greetOverTime(GreetingRequest greetingRequest) {
		return Flux.fromStream(Stream
			.generate(() -> new GreetingResponse("Hello " + greetingRequest.getName()
				+ " @ " + Instant.now() + " !")))
			.delayElements(Duration.ofSeconds(1));
	}
}
