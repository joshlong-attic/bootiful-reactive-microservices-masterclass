package rsb.orchestration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String args[]) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}

@RequestMapping("/orders")
@RestController
class OrderRestController {

	// customerId -> orders
	private final Map<Integer, List<Order>> orders = //
			IntStream//
					.range(0, 10)//
					.boxed()//
					.map(id -> Map.entry(id, new CopyOnWriteArrayList<Order>()))
					.collect(Collectors.toConcurrentMap(Map.Entry::getKey, e -> {
						var listOfOrders = e.getValue();
						var max = (int) (Math.random() * 10);
						if (max < 1) {
							max = 1;
						}
						for (var i = 0; i < max; i++) {
							listOfOrders.add(
									new Order(UUID.randomUUID().toString(), e.getKey()));
						}
						return listOfOrders;
					}));

	@GetMapping
	Flux<Order> orders(@RequestParam(required = false) Integer[] ids) {
		var customerStream = this.orders.keySet().stream();
		var includedCustomerIds = Arrays.asList(ids);
		var orderStream = customerStream.filter(includedCustomerIds::contains)//
				.flatMap(id -> this.orders.get(id).stream());
		return Flux.fromStream(orderStream);
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {

	private String id;

	private Integer customerId;

}