package rsb.testing.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

@RunWith(SpringRunner.class) 
@DataMongoTest 
public class CustomerRepositoryTest {

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	public void findByName() {
		String commonName = "Jane";
		Customer one = new Customer("1", commonName);
		Customer two = new Customer("2", "John");
		Customer three = new Customer("3", commonName);

		Publisher<Customer> setup = this.customerRepository //
				.deleteAll() //
				.thenMany(this.customerRepository.saveAll(Flux.just(one, two, three))) //
				.thenMany(this.customerRepository.findByName(commonName));

		Predicate<Customer> customerPredicate = customer -> commonName.equalsIgnoreCase(customer.getName());

		StepVerifier 
				.create(setup) //
				.expectNextMatches(customerPredicate) //
				.expectNextMatches(customerPredicate) //
				.verifyComplete();

	}

}
