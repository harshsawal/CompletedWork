package com.amica.billing.db;

import java.util.stream.Stream;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.amica.billing.Update;


/**
 * Spring Data repository for {@link Update} objects.
 * 
 * @author Will Provost
 */
public interface HistoryRepository extends PagingAndSortingRepository<Update,String> {
	public Stream<Update> streamAllBy();
}
