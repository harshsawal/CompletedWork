package com.amica.billing.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

import com.amica.billing.Customer;
import com.amica.billing.Terms;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Functional test for the Billing web service, focused on the 
 * /customers root URL. This test is not repeatable, because
 * unlike the integration tests it can't reset the database
 * underneath the running service (which caches data in memory).
 *  
 * @author Will Provost
 */
public class CustomerControllerRestTemplateTest extends RestTemplateTestBase {
	
	@Data
	@NoArgsConstructor
	public static class CustomerAndVolume {
		private Customer customer;
		private double volume;
	}
	
	protected String getResourceName() {
		return "customers";
	}

	@Test
	public void testGetCustomer() {
		Customer customer = template.getForObject
				(subResource("Roy Clark"), Customer.class);
		assertThat(customer, samePropertyValuesAs
				(new Customer("Roy", "Clark", Terms.CREDIT_60)));
	}
	
	@Test
	public void testGetCustomerNonExistent() {
		assertThrows(HttpClientErrorException.NotFound.class, 
			() -> template.getForObject(subResource("Omar Khayyam"), 
				Customer.class));
	}
	
	@Test
	public void testCreateCustomer() {
		final String FIRST = "William";
		final String LAST = "Fitzwilliam";
		final Terms TERMS = Terms.CREDIT_60;
		
		template.put(service(), new Customer(FIRST, LAST, TERMS));
		Customer customer = template.getForObject
				(subResource(FIRST + " " + LAST), Customer.class);
		assertThat(customer, samePropertyValuesAs
				(new Customer(FIRST, LAST, TERMS)));
	}
	
	@Test
	public void testCreateCustomerWithDuplicateName() {
		assertThrows(HttpClientErrorException.Conflict.class,
			() -> template.put(service(), 
				new Customer("Roy", "Clark", Terms.CASH)));
	}
	
	@Test
	public void testGetCustomersByVolume() {
		CustomerAndVolume[] customers = template.getForObject
				(queryParam("byVolume"), CustomerAndVolume[].class);
		assertThat(customers[0].getCustomer().getName(), equalTo("Jerry Reed"));
	}
}
