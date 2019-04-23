package com.example.client.spring;

import com.example.client.GreetingRequest;
import com.example.client.GreetingResponse;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class GreetingClient {

	private final RSocketRequester requester;

	Publisher<GreetingResponse> greet(String name) {
		return this.requester
			.route("greetings")
			.data(new GreetingRequest(name))
			.retrieveFlux(GreetingResponse.class);
	}

}
