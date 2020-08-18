package org.acme.getting.started;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.MultiHelper;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;

@ApplicationScoped
public class ReactiveGreetingService {

    @Inject
    EventBus eventBus;

    public Uni<String> greeting(String name) {
        return Uni.createFrom().item(name)
                .onItem().apply(n -> String.format("hello %s", name));
    }


    @ConsumeEvent("notifications")
    public void notificationProducer(String msg) {
        //Persist to the DB + "Declared availibility"
        eventBus.publish("display", "Re-publish event: " + msg);
    }


    public Multi<String> displayConsumer() {
        Multi<Integer> multi = eventBus.<String>consumer("display").toMulti()
                .onItem().transform(Message::body);

        return multi;
    }


    public Multi<String> greetings(int count, String name) {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                .onItem().apply(n -> String.format("hello %s - %d", name, n))
                .transform().byTakingFirstItems(count);
    }


    public void fireEvent(String event) {
        eventBus.publish("notifications", "Declared availibility --> " + event);
    }
}
