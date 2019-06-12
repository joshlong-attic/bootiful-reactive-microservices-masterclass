package com.example.producer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;


@WebFluxTest(value = {ReservationHttpConfiguration.class})
@RunWith(SpringRunner.class)
public class ReservationHttpTest {

	@MockBean
	private ReservationRepository repository;

	@Autowired
	private WebTestClient client;

	@Test
	public void getAllReservations() throws Exception {

		Mockito.when(this.repository.findAll()).thenReturn(Flux.just(new Reservation("1", "Jane")));

		this.client
			.get()
			.uri("/reservations")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
			.expectBody().jsonPath("@.[0].name").isEqualTo("Jane");

	}
}
