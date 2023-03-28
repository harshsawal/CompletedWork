package com.amica.billing.ws;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Base class for our functional tests that use the WebClient API.
 *  
 * @author Will Provost
 */
public abstract class WebClientTestBase {

	protected abstract String getResourceName();
	
	protected String root() {
		return "http://localhost:8081/";
	}
	
	protected String service() {
		return root() + getResourceName();
	}
	
	protected WebClient client;
	
	@BeforeEach
	public void setUp() {
		client = WebClient.builder().baseUrl(service()).build();
	}
	
	protected <T> T getOne(String URL, Class<T> entityClass) {
		return client.get(
				).uri(URL)
				.retrieve()
				.bodyToMono(entityClass)
				.block();
	}
	
	protected <T> List<T> getMany(String URL, Class<T> entityClass) {
		return client.get()
				.uri(URL)
				.retrieve()
				.bodyToFlux(entityClass)
				.collectList()
				.block();
	}
	
	protected <T> T post(T obj, Class<T> entityClass) {
		return client.post()
				.body(Mono.just(obj), entityClass)
				.retrieve()
				.bodyToMono(entityClass)
				.block();
	}
	
	protected <T,U> U post(T obj, Class<T> requestClass, Class<U> responseClass) {
		return client.post()
				.body(Mono.just(obj), requestClass)
				.retrieve()
				.bodyToMono(responseClass)
				.block();
	}

	protected <T,U> U post(String URL, T obj, 
			Class<T> requestClass, Class<U> responseClass) {
		return client.post()
				.uri(URL)
				.body(Mono.just(obj), requestClass)
				.retrieve()
				.bodyToMono(responseClass)
				.block();
	}

	protected <T> void put(T obj, Class<T> entityClass) {
		client.put()
				.body(Mono.just(obj), entityClass)
				.retrieve()
				.toBodilessEntity()
				.block();
	}
	
	protected <T> void put(String URL, T obj, Class<T> entityClass) {
		client.put()
				.uri(URL)
				.body(Mono.just(obj), entityClass)
				.retrieve()
				.toBodilessEntity()
				.block();
	}
	
	protected <T> T patch(String URL, Class<T> entityClass) {
		return client.patch()
				.uri(URL)
				.retrieve()
				.bodyToMono(entityClass)
				.block();
	}
	
	protected <T> T patch(String URL, T obj, Class<T> entityClass) {
		return client.patch()
				.uri(URL)
				.body(Mono.just(obj), entityClass)
				.retrieve()
				.bodyToMono(entityClass)
				.block();
	}
	
	protected <T> void delete(T obj, Class<T> entityClass) {
		client.delete()
				.retrieve()
				.toBodilessEntity()
				.block();
	}
}
