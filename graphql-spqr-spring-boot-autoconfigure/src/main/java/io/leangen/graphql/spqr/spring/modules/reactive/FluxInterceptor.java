package io.leangen.graphql.spqr.spring.modules.reactive;

import io.leangen.graphql.execution.InvocationContext;
import io.leangen.graphql.execution.ResolverInterceptor;
import io.leangen.graphql.spqr.spring.web.reactive.WebFluxContext;
import reactor.core.publisher.Flux;

public class FluxInterceptor implements ResolverInterceptor {

    @Override
    public Object aroundInvoke(InvocationContext context, Continuation continuation) throws Exception {
        WebFluxContext reactiveContext = (WebFluxContext) context.getResolutionEnvironment().rootContext;
        return ((Flux<?>) continuation.proceed(context)).subscriberContext(reactiveContext.getSubscriberContext());
    }
}
