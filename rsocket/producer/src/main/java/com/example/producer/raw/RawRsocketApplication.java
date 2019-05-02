package com.example.producer.raw;

import com.example.GreetingService;
import com.example.producer.GreetingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.*;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@SpringBootApplication
public class RawRsocketApplication {

	private final ObjectMapper objectMapper;

	@Bean
	GreetingService greetingService() {
		return new GreetingService();
	}

	public RawRsocketApplication(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void go() throws Exception {

		GreetingService greetingService = this.greetingService();

		RSocketFactory
			.receive()
			.acceptor((setup, requestRsocket) -> {

				RSocket responseRsocket = new AbstractRSocket() {
					@Override
					public Flux<Payload> requestStream(Payload payload) {
						return greetingService
							.greetOverTime(new GreetingRequest(payload.getDataUtf8()))
							.map(RawRsocketApplication.this::from)
							.map(DefaultPayload::create);
					}
				};
				return Mono.just(responseRsocket);
			})
			.transport(TcpServerTransport.create(7000))
			.start()
			.subscribe();

	}

	@SneakyThrows
	private String from(Object gr) {
		return objectMapper.writeValueAsString(gr);
	}

	public static void main(String[] args) throws IOException {
		SpringApplication.run(RawRsocketApplication.class);
		System.in.read();
	}
}
