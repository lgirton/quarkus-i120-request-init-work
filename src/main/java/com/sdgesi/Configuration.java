package com.sdgesi;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
public class Configuration {

    @ConfigProperty(name = "max.redeliveries", defaultValue = "20")
    int maximumRedeliveries;

    @ConfigProperty(name = "initial.redelivery.delay", defaultValue = "5000")
    int initialRedeliveryDelay;

    @ConfigProperty(name = "redelivery.delay", defaultValue = "5000")
    int redeliveryDelay;

    @ConfigProperty(name = "broker.url", defaultValue = "tcp://broker-amq:61616")
    String brokerURL;


    @Inject
    CamelContext context;

    @Named
    public ActiveMQComponent activemq() {
        ActiveMQComponent component =
                ComponentsBuilderFactory
                        .activemq()
                        .brokerURL(brokerURL)
                        .errorHandlerLogStackTrace(false)
                        .build();

        component.setCamelContext(context);
        component.setMaxConcurrentConsumers(5);

        configureRedeliveryPolicy(component);

        return component;
    }


    private void configureRedeliveryPolicy(ActiveMQComponent component) {
        PooledConnectionFactory connectionFactory = (PooledConnectionFactory) component.getOrCreateConnectionFactory();

        ActiveMQConnectionFactory acf = (ActiveMQConnectionFactory) connectionFactory.getConnectionFactory();

        RedeliveryPolicy redeliveryPolicy = acf.getRedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(maximumRedeliveries);
        redeliveryPolicy.setInitialRedeliveryDelay(initialRedeliveryDelay);
        redeliveryPolicy.setRedeliveryDelay(redeliveryDelay);
    }
}
