package io.leangen.graphql.spqr.spring.web.dto;

public class ExecutorParams<R> {

    public final GraphQLRequest graphQLRequest;
    public final R request;
    public final TransportType transportType;

    public ExecutorParams(GraphQLRequest graphQLRequest, R request, TransportType transportType) {
        this.graphQLRequest = graphQLRequest;
        this.request = request;
        this.transportType = transportType;
    }
}
