package com.example.producer;


import org.assertj.core.api.Assertions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

public class ReservationTest {

	private Matcher<String> matcher = new BaseMatcher<>() {

		@Override
		public void describeTo(Description description) {
			description.appendText("the first letter of the name should be uppercase");
		}

		@Override
		public boolean matches(Object item) {
			Assert.assertTrue(item instanceof String);
			String str = (String) item;
			return Character.isUpperCase(str.charAt(0));
		}
	};

	@Test
	public void create() throws Exception {
		Reservation re = new Reservation("1", "Name");
		Assert.assertEquals(re.getName(), "Name");
		Assert.assertThat(re.getName(), matcher);
		Assertions.assertThat(re.getName()).isNotEmpty();
		Assertions.assertThat(re.getName()).isEqualToIgnoringCase("name");
	}

}
