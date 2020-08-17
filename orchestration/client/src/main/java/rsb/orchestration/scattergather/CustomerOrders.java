package rsb.orchestration.scattergather;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.Profile;

import java.util.Collection;

@Data
@RequiredArgsConstructor
class CustomerOrders {

	private final Customer customer;

	private final Collection<Order> orders;

	private final Profile profile;

}
