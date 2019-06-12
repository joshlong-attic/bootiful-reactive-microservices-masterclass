package com.example.consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureStubRunner(
	ids = "com.example:producer:+:8080",
	stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
//@AutoConfigureWireMock(port = 8080)
public class ConsumerApplicationTests {

	@Autowired
	private ReservationClient client;

//	private String buildJsonFor(String id, String name) {
//		return " {\"reservationName\":\"" + name + "\",\"id\":\"" + id + "\"}";
//	}

	@Before
	public void before() {

//
//		var json = "[" + buildJsonFor("1", "Jane") + "," + buildJsonFor("2", "John") + "]";
//		stubFor(get(
//			urlEqualTo("/reservations"))
//			.willReturn(aResponse()
//				.withBody(json)
//				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//				.withStatus(HttpStatus.OK.value())
//			));

	}

	@Test
	public void contextLoads() {

		var allReservations = this.client.getAllReservations();
		StepVerifier
			.create(allReservations)
			.expectNext(new Reservation("1", "Jane"))
			.expectNext(new Reservation("2", "John"))
			.verifyComplete();
	}

}
