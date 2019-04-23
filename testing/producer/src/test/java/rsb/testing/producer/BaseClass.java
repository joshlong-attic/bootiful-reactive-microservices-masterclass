package rsb.testing.producer;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.port=0")
public class BaseClass {

	@LocalServerPort
	private int port;


	@MockBean
	private CustomerRepository customerRepository;

	@Autowired
	private RouterFunction<?>[] routerFunctions;

	@Before
	public void before() throws Exception {

		Mockito.when(this.customerRepository.findAll()).thenReturn(
				Flux.just(new Customer("1", "Jane"), new Customer("2", "John")));

		RestAssuredWebTestClient.standaloneSetup(this.routerFunctions);
	}


	@Configuration
	@Import(ProducerApplication.class)
	public static class TestConfiguration {
	}
}
