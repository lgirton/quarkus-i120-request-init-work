package com.sdgesi;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.component.jms.JmsQueueEndpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Session;

@ApplicationScoped
public class IncomingRouteBuilder extends RouteBuilder {
    private int maximumRedeliveries = 2;
    private int initialRedeliveryDelay = 5000;
    private int redeliveryDelay = 5000;
    private String destinationName = "incoming.messages";
    private String brokerURL = "tcp://localhost:61616";

    @Override
    public void configure() throws Exception {
        rest("/incoming")
                .id("incoming")
                .post()
                .to("direct:enqueue");


        from("direct:enqueue")
                .id("enqueue")
                .to("activemq:queue:incoming.messages")
                .transform()
                .simple("Hello");

        JmsQueueEndpoint endpoint = createJMSQueueEndpoint();


        from(endpoint)
                .id("dequeue")
                .to("rest:post:outgoing?host=localhost:9000&bridgeEndpoint=true");


    }

    private JmsQueueEndpoint createJMSQueueEndpoint() throws Exception {
        ActiveMQComponent component = ActiveMQComponent
                .activeMQComponent(brokerURL);

        component.setCamelContext(getContext());

        PooledConnectionFactory connectionFactory = (PooledConnectionFactory) component.getOrCreateConnectionFactory();
        ActiveMQConnectionFactory acf = (ActiveMQConnectionFactory) connectionFactory.getConnectionFactory();
        RedeliveryPolicy redeliveryPolicy = acf.getRedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(maximumRedeliveries);
        redeliveryPolicy.setInitialRedeliveryDelay(initialRedeliveryDelay);
        redeliveryPolicy.setRedeliveryDelay(redeliveryDelay);

        JmsQueueEndpoint endpoint = (JmsQueueEndpoint) component.createEndpoint("activemq:queue");
        endpoint.setDestinationName(destinationName);
        endpoint.setAcknowledgementMode(Session.CLIENT_ACKNOWLEDGE);
        return endpoint;
    }
}
