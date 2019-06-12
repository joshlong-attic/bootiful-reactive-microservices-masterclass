package com.example.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import reactor.test.StepVerifier;

@SpringBootTest
@AutoConfigureStubRunner(
	ids = "com.example:producer:+:8080",
	stubsMode = StubRunnerProperties.StubsMode.REMOTE
)
//@AutoConfigureWireMock(port = 8080)
public class ConsumerApplicationTests {

	@Autowired
	private ReservationClient client;

//	private String buildJsonFor(String id, String name) {
//		return " {\"reservationName\":\"" + name + "\",\"id\":\"" + id + "\"}";
//	}

	@Test
	public void contextLoads() {
//
//		var json = "[" + buildJsonFor("1", "Jane") + "," + buildJsonFor("2", "John") + "]";
//		stubFor(get(
//			urlEqualTo("/reservations"))
//			.willReturn(aResponse()
//				.withBody(json)
//				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//				.withStatus(HttpStatus.OK.value())
//			));

		var allReservations = this.client.getAllReservations();
		StepVerifier
			.create(allReservations)
			.expectNext(new Reservation("1", "Jane"))
			.expectNext(new Reservation("2", "John"))
			.verifyComplete();
	}

}
