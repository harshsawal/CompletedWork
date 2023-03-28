package com.amica.billing.db;

import java.util.stream.Stream;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.amica.billing.Invoice;

/**
 * Spring Data repository for {@link Invoice} objects.
 * 
 * @author Will Provost
 */
public interface InvoiceRepository extends PagingAndSortingRepository<Invoice,Integer> {
	
	/**
	 * Alternative to the built-in findAll() that returns a stream.
	 */
	public Stream<Invoice> streamAllBy();
}
