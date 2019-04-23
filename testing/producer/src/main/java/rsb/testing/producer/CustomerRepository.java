package rsb.testing.producer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

	Flux<Customer> findByName(String name);

}
