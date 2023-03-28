package com.amica.billing.ws;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for our functional tests. Sets up a RestTemplate
 * and offers a handful of helper functions for URL-building.
 * Derived classes must supply the base resource name, and may supply their own, 
 * preferred message converter, the default being the Jackson/JSON converter.
 *  
 * @author Will Provost
 */
public abstract class RestTemplateTestBase {

	protected abstract String getResourceName();
	
	protected String root() {
		return "http://localhost:8081/";
	}
	
	protected String service() {
		return root() + getResourceName();
	}
	
	protected String subResource(String URL) {
		return service() + "/" + URL;
	}
	
	protected String queryParam(String queryString) {
		return service() + "?" + queryString;
	}
	
	protected RestTemplate template;
	
	protected HttpMessageConverter<?> getConverter() {
		return new MappingJackson2HttpMessageConverter();
	}
	
	@BeforeEach
	public void setUp() {
		HttpComponentsClientHttpRequestFactory requestFactory = 
				new HttpComponentsClientHttpRequestFactory();
		template = new RestTemplate(requestFactory);
		
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(getConverter());
		template.setMessageConverters(converters);
	}
}
