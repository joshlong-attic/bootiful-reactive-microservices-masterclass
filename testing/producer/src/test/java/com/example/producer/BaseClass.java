package com.example.producer;


import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

@SpringBootTest(classes = {
	ProducerApplication.class,
	ReservationHttpConfiguration.class},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = "server.port=0")
@RunWith(SpringRunner.class)
public class BaseClass {

	@MockBean
	private ReservationRepository repository;

	@LocalServerPort
	int port;

	@Before
	public void before() {
		Mockito.when(this.repository.findAll()).thenReturn(Flux.just(new Reservation("1", "Jane"),
			new Reservation("2", "John")));
		RestAssured.baseURI = "http://localhost:" + this.port;
	}
}
