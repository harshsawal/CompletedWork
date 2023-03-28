package com.amica.billing.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amica.billing.Customer;
import com.amica.billing.Terms;
import com.amica.billing.TestUtility.IntegrationConfig;
import com.amica.billing.db.Migration;
import com.amica.billing.db.mongo.MongoPersistence;
import com.amica.billing.ws.Exceptions.CustomerNameConflictException;
import com.amica.billing.ws.Exceptions.CustomerNotFoundException;

/**
 * Integration test for the {@link CustomerController}.
 * We use the test database, and reset the data before each test.
 *
 * @author Will Provost
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=IntegrationConfig.class)
public class CustomerControllerIntegrationTest {

	@Autowired
	private CustomerController controller;

	@Autowired
	private Migration migration;
	
	@Autowired
	private MongoPersistence persistence;

    @BeforeEach
    public void setUp() {
    	migration.migrate();
    	persistence.load();
    }

    @Test
	public void testGetCustomer() {
		Customer customer = controller.getCustomer("Roy Clark");
		assertThat(customer.getFirstName(), equalTo("Roy"));
		assertThat(customer.getLastName(), equalTo("Clark"));
		assertThat(customer.getTerms(), equalTo(Terms.CREDIT_60));
	}

	@Test
	public void testGetCustomerNonExistent() {
		assertThrows(CustomerNotFoundException.class, 
				() -> controller.getCustomer("Omar Khayyam"));
	}

	@Test
	public void testCreateCustomer() {
		final String firstName = "William";
		final String lastName = "Fitzwilliam";
		final Terms terms = Terms.CASH;
		final String name = firstName + " " + lastName;
		final Customer newCustomer = new Customer(firstName, lastName, terms);
		
		controller.createCustomer(newCustomer);
		Customer retrieved = persistence.getCustomers().get(name);
		assertThat(retrieved.getFirstName(), equalTo(firstName));
		assertThat(retrieved.getLastName(), equalTo(lastName));
		assertThat(retrieved.getTerms(), equalTo(terms));
	}

	@Test
	public void testCreateCustomerWithDuplicateName() {
		assertThrows(CustomerNameConflictException.class, 
			() -> controller.createCustomer 
			(new Customer("Roy", "Clark", Terms.CASH)));
	}

	@Test
	public void testGetCustomersByVolume() {
		assertThat(controller.getCustomersAndVolume().findFirst().get()
				.getCustomer().getName(), equalTo("Jerry Reed"));
	}
}
