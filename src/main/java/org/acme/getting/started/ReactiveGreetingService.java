package org.acme.getting.started;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.MultiCreate;
import io.vertx.axle.PublisherHelper;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;

@ApplicationScoped
public class ReactiveGreetingService {

    @Inject
    EventBus bus;

    public Uni<String> greeting(String name) {
        return Uni.createFrom().item(name)
                .onItem().apply(n -> String.format("hello %s", name));
    }


    @ConsumeEvent("notifications")
    public void notificationProducer(String msg) {
        //Persist to the DB + "Declared availibility"
        bus.publish("display", "Re-publish event: " + msg);
    }

//    @ConsumeEvent("display")
//    public Multi<String> displayConsumer(String msg) {
//        System.out.println("Display Event received: " + msg);
//       return Multi.createFrom().item(msg);
//        //Persist to the DB + "Declared availibility"
//    }

    public Multi<String> displayConsumer() {
     final MultiCreate from = Multi.createFrom();
     final MessageConsumer<String> consumer = bus.<String>consumer("display");
     return from.publisher(PublisherHelper.toPublisher(consumer.bodyStream()));
        //Persist to the DB + "Declared availibility"
    }


    public Multi<String> greetings(int count, String name) {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                .onItem().apply(n -> String.format("hello %s - %d", name, n))
                .transform().byTakingFirstItems(count);
    }


    public void fireEvent(String event) {
        bus.publish("notifications", "Declared availibility --> " +event );
    }
}
