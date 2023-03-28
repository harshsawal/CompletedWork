package com.amica.billing.ws;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.amica.billing.Billing;
import com.amica.billing.db.CustomerRepository;

/**
 * Spring Boot application that hosts two REST controllers.
 *
 * @author Will Provost
 */
@Configuration
@ComponentScan(basePackageClasses=Billing.class)
@EnableAutoConfiguration
@EnableMongoRepositories(basePackageClasses=CustomerRepository.class)
@EnableScheduling
@PropertySource("classpath:service.properties")
@EnableJms
public class BillingRS {
	
//	public static final String CONFIGURATION_NAME = "MQ";
//	public static final String INTEGRATION_QUALIFIER = "T1";
	
	@Value("${spring.activemq.broker-url}")
	public String brokerURL;

//	@Bean
//	public Configuration configuration() {
//		ComponentConfigurationManager.getInstance().initialize();
//		return ComponentConfigurationManager.getInstance()
//				.getConfiguration(CONFIGURATION_NAME);
//	}

	/**
	 * Creates a connection factory with the injected broker URL.
	 */
	@Bean
	public ConnectionFactory connectionFactory() {
		return new ActiveMQConnectionFactory(brokerURL);
	}

	/**
	 * Creates a JMS template with our connection-factory bean.
	 */
	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate template = new JmsTemplate(connectionFactory());
		template.setPubSubDomain(true);
		return template;
	}

	/**
	 * Creates a JMS template with our connection-factory bean.
	 */
	@Bean
	public JmsListenerContainerFactory<? extends MessageListenerContainer>
	jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
	    DefaultJmsListenerContainerFactory factory =
	    		new DefaultJmsListenerContainerFactory();
	    factory.setConnectionFactory(connectionFactory);
	    factory.setPubSubDomain(true);
	    return factory;
	}
	
	
	public static void main(String[] args) {

		// We enable Sprint Boot's shutdown hook, in order to support automated
		// testing of the course software. You wouldn't generally do this:
		//System.setProperty("management.endpoints.web.exposure.include", "*");
		//System.setProperty("management.endpoint.shutdown.enabled", "true");
		//System.setProperty("endpoints.shutdown.enabled", "true");
	
		SpringApplication.run(BillingRS.class, args);
	}
}
