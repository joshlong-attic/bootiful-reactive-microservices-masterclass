package rsb.orchestration.scattergather;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.Profile;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class CrmClient {

	private final WebClient http;

	Mono<Profile> getProfile(Integer customerId) {
		var profilesRoot = "http://profile-service/profiles/{id}";
		return http.get().uri(profilesRoot, customerId).retrieve()
				.bodyToMono(Profile.class);
	}

	Flux<Customer> getCustomers(Integer[] ids) {
		var customersRoot = "http://customer-service/customers?ids="
				+ buildStringForIds(ids);
		return http.get().uri(customersRoot).retrieve().bodyToFlux(Customer.class);
	}

	Flux<Order> getOrders(Integer[] ids) {
		var ordersRoot = "http://order-service/orders?ids=" + buildStringForIds(ids);
		return http.get().uri(ordersRoot).retrieve().bodyToFlux(Order.class);
	}

	private String buildStringForIds(Integer[] ids) {
		return Arrays.stream(ids).map(id -> Integer.toString(id))
				.collect(Collectors.joining(","));
	}

}
