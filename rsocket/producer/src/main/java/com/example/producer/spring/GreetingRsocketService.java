package com.example.producer.spring;

import com.example.GreetingService;
import com.example.producer.GreetingRequest;
import com.example.producer.GreetingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
class GreetingRsocketService {

	private final GreetingService greetingService;

	@MessageMapping("greetings")
	Flux<GreetingResponse> greet(GreetingRequest name) {
		return greetingService.greetOverTime(name);
	}
}
