package com.example.client.spring;

import com.example.client.GreetingResponse;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ClientApplication {

	@Bean
	RSocket rSocket() {
		return
			RSocketFactory
				.connect()
				.frameDecoder(PayloadDecoder.ZERO_COPY)
				.dataMimeType(MimeTypeUtils.APPLICATION_JSON_VALUE)
				.transport(TcpClientTransport.create(7000))
				.start()
				.block();
	}

	@Bean
	RouterFunction<ServerResponse> routes(GreetingClient client) {
		return route()
			.GET("/greetings/{name}", serverRequest -> ok()
				.contentType(MediaType.TEXT_EVENT_STREAM)
				.body(client.greet(serverRequest.pathVariable("name")), GreetingResponse.class))
			.build();
	}

	@Bean
	RSocketRequester requester(RSocketStrategies strategies) {
		return RSocketRequester.create(rSocket(), MimeTypeUtils.APPLICATION_JSON, strategies);
	}

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}
}
