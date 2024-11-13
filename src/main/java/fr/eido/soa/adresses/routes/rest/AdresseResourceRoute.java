package fr.eido.soa.adresses.routes.rest;

import fr.eido.soa.adresses.model.InboundNameAddress;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.net.ConnectException;

@Component
public class AdresseResourceRoute  extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        restConfiguration().component("jetty").host("0.0.0.0").port(8080).bindingMode(RestBindingMode.json).enableCORS(true)  ;


        // Handle connexion message
        onException(JMSException.class, ConnectException.class)
                .handled(true)
                .log(LoggingLevel.INFO, "JMS connection could not be established");

        // Rest api route
        // persists data in Mysql base via Jpa
        // Send data to ActiveMQ queue
        rest("masterclass")
                .produces("application/json")
                .post("nameAddress").type(InboundNameAddress.class).route().routeId("newRestRouteId")
                .log(LoggingLevel.INFO, String.valueOf(simple("${body}")))
                .to("direct:persistMessage")
                .wireTap("seda:sendToQueue")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .transform().simple("Message Processed: ${body}")
                .endRest();

        // Timer to start route
        from("timer:startBatch?repeatCount=1")
                .routeId("timerRunOnceId")
                .to("controlbus:route?routeId=batchMessageRouteId&action=start");

        // Route to persiste in data base
        from("direct:persistMessage")
                .routeId("persistMessageRouteId")
                .to("jpa:"+InboundNameAddress.class.getName());

        // Route to send in active mq queue
        from("seda:sendToQueue")
                .routeId("sendToQueueRouteId")
                .to("activemq:queue:nameaddressqueue");
    }
}
