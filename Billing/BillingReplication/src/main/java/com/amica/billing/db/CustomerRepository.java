package com.amica.billing.db;

import java.util.stream.Stream;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.amica.billing.Customer;

/**
 * Spring Data repository for {@link Customer} objects.
 * 
 * @author Will Provost
 */
public interface CustomerRepository extends PagingAndSortingRepository<Customer,String> {

	/**
	 * Alternative to the built-in findAll() that returns a stream.
	 */
	public Stream<Customer> streamAllBy();
}
