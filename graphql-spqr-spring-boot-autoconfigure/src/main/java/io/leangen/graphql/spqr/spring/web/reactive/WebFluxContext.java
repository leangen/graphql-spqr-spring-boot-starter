package io.leangen.graphql.spqr.spring.web.reactive;

import io.leangen.graphql.spqr.spring.autoconfigure.DefaultGlobalContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.util.context.Context;

public class WebFluxContext extends DefaultGlobalContext<ServerWebExchange> {

    private final Context subscriberContext;

    public WebFluxContext(ServerWebExchange request, Context subscriberContext) {
        super(request);
        this.subscriberContext = subscriberContext;
    }

    public Context getSubscriberContext() {
        return subscriberContext;
    }
}
