package rsb.testing.producer;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class CustomerTest {

	@Test
	public void create() {
		Customer customer = new Customer("123", "foo"); 
		Assert.assertEquals(customer.getId(), "123");  
		Assert.assertThat(customer.getId(), Matchers.is("123")); 
		Assertions.assertThat(customer.getName()).isEqualToIgnoringWhitespace("foo"); 
	}

}
