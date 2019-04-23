package com.example.client.raw;

import com.example.client.GreetingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
class GreetingClient {

	private final ObjectMapper objectMapper;

	Flux<GreetingResponse> greet(String name) {
		return RSocketFactory
			.connect()
			.transport(TcpClientTransport.create(7000))
			.start()
			.flatMapMany(rs -> rs.requestStream(DefaultPayload.create(name))
				.map(Payload::getDataUtf8)
				.map(this::from)
			);
	}

	@SneakyThrows
	private GreetingResponse from(String json) {
		return objectMapper.readValue(json, GreetingResponse.class);
	}
}
