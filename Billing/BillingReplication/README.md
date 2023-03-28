@@ Billing Replication Workshop

1. Add dependencies for Spring Boot JMSwith Active MQ, and the ActiveMQ client itself. You can find these int he Spring/Messages POM.

1. Add the property for the location o the ActiveMQ broker to your server.properties

spring.activemq.broker-url = tcp://workspot-c-094:61616

1. In BillingRS, @EnableJms.

1. In BillingRS, inject the broker URL and configure a connection factory, JMS template, and listener-container factory. Get the code for this from the publishing and subscribing applications in Spring/Messages.

1. Add a component com.amica.billing.mq.Replication. Remaining instructions are about code in that class ...

1. Define a constant TOPIC with the value "BillingReplication".

1. Autowire a Persistence reference.

1. Define a field parser and initialize to a new CSVParser.

1. Define a @JmsListener method, with TOPIC as the destination, taking a string parameter. It should use the CSVParser to parse the message text as a Customer; then, if that customer doesn't yet exist in the database (check findById(name)), then save() it. Print a message to the console saying you either saved it or ignored it.

1. If anyone else has gotten to the point of publishing messages, you could test now: run your web service, and see if you can replicate their new customers. You should see your console output, and you can check by GETting the new customer by name or by GETting all customers.

1. Autowire a Billing and a JmsTemplate.

1. Add a method that takes a Customer, uses the parser to format it as a string, and publishes a message with that string as its content. Print to the console, saying that you've published the message.

1. Add a @PostConstruct method that subscribes to the Billing component's Customer-related events, passing a reference to your new method.

1. At this point you can test (again?) and try PUTting a customer to your running service. You should see your console output: publishing and then ignoring the resulting inbound message; and others should see your new customer and add it.

1. How many BillingRS applications can we get linked in this way?


