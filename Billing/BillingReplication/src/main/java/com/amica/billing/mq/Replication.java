package com.amica.billing.mq;

import javax.annotation.PostConstruct;

import org.checkerframework.com.github.javaparser.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.amica.billing.Billing;
import com.amica.billing.Customer;
import com.amica.billing.db.Persistence;
import com.amica.billing.parse.CSVParser;

@Component
public class Replication {

	public static final String TOPIC = "BillingReplication";
	
	@Autowired
	private Persistence persistence;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private Billing billing;
	
	private CSVParser parser = new CSVParser();
	
	
	@SuppressWarnings("unlikely-arg-type")
	@JmsListener(destination=TOPIC)
    public void onMessage(String text) {
        System.out.println("Received message: " + text);
        Customer customer = parser.parseCustomer(text);
        if(persistence.getCustomers().containsKey(customer)) {
        	Log.info(String.format("%s : %s", "Customer already exists!", customer.getName()));
        } else {
        	persistence.saveCustomer(customer);
        	Log.info(String.format("%s : %s", "Customer successfully saved!", customer.getName()));
        }
    }
	
	
	public void publishMessage(Customer customer) {
		String customerText = parser.formatCustomer(customer);
		jmsTemplate.convertAndSend(TOPIC, customerText);
		System.out.println("Published message: " + customerText);
	}
	
	@PostConstruct
	public void subscribe() {
		billing.addCustomerListener(this::publishMessage);
	}
	
}
