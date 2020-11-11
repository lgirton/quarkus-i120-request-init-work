package com.sdgesi;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IncomingRouteBuilder extends RouteBuilder {

    @ConfigProperty(name = "log.exhausted", defaultValue = "false")
    boolean logExhausted;

    @ConfigProperty(name = "queue.name", defaultValue = "incoming.messages")
    String destinationName;

    @ConfigProperty(name = "rest.uri", defaultValue = "outgoing")
    String restUri;

    @ConfigProperty(name = "rest.host", defaultValue = "localhost:18080")
    String restHost;

    @Override
    public void configure() {

        onException(Exception.class)
                .logExhausted(logExhausted);

        rest("/incoming")
                .id("incoming")
                .post()
                .to("direct:enqueue");

        from("direct:enqueue")
                .id("enqueue")
                .onException(Exception.class)
                    .maximumRedeliveries(0)
                    .handled(true)
                    .transform()
                    .simple("Error")
                .end()
                .toF("activemq:queue:%s", destinationName)
                .transform()
                .simple("Hello");

        fromF("activemq:queue:%s?acknowledgementModeName=CLIENT_ACKNOWLEDGE", destinationName)
                .id("dequeue")
                .toF("rest:post:%s?host=%s&bridgeEndpoint=true", restUri, restHost);


    }


}
