package io.leangen.graphql.spqr.spring.web.mvc;

import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import org.springframework.web.context.request.NativeWebRequest;

@FunctionalInterface
public interface GraphQLMvcExecutor extends GraphQLExecutor<NativeWebRequest> {
}
