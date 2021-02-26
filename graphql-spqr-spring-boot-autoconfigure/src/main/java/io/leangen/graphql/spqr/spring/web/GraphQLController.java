package io.leangen.graphql.spqr.spring.web;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.dto.ExecutorParams;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import io.leangen.graphql.spqr.spring.web.dto.TransportType;
import io.leangen.graphql.util.Utils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public abstract class GraphQLController<R> {

    protected final GraphQL graphQL;
    protected final GraphQLExecutor<R> executor;

    public GraphQLController(GraphQL graphQL, GraphQLExecutor<R> executor) {
        this.graphQL = graphQL;
        this.executor = executor;
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Object executeJsonPost(@RequestBody GraphQLRequest requestBody,
                                          GraphQLRequest requestParams,
                                          R request) {
        return jsonPost(requestBody, requestParams, request, TransportType.HTTP);
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Object executeJsonPostEventStream(@RequestBody GraphQLRequest requestBody,
                                  GraphQLRequest requestParams,
                                  R request) {
        return jsonPost(requestBody, requestParams, request, TransportType.HTTP_EVENT_STREAM);
    }

    public Object jsonPost(GraphQLRequest requestBody, GraphQLRequest requestParams, R request, TransportType transportType) {
        String id = Utils.isNotEmpty(requestParams.getId()) ? requestParams.getId() : requestBody.getId();
        String query = Utils.isNotEmpty(requestParams.getQuery()) ? requestParams.getQuery() : requestBody.getQuery();
        String operationName = Utils.isNotEmpty(requestParams.getOperationName()) ? requestParams.getOperationName() : requestBody.getOperationName();
        Map<String, Object> variables = requestParams.getVariables().isEmpty() ? requestBody.getVariables() : requestParams.getVariables();
        ExecutorParams<R> params = new ExecutorParams<>(new GraphQLRequest(id, query, operationName, variables), request, transportType);
        return executor.execute(graphQL, params);
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {"application/graphql", "application/graphql;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Object executeGraphQLPost(@RequestBody String queryBody,
                                   GraphQLRequest originalReq,
                                   R request) {
        String query = Utils.isNotEmpty(originalReq.getQuery()) ? originalReq.getQuery() : queryBody;
        GraphQLRequest remappedReq = new GraphQLRequest(originalReq.getId(), query, originalReq.getOperationName(), originalReq.getVariables());
        ExecutorParams<R> params = new ExecutorParams<>(remappedReq, request, TransportType.HTTP);
        return executor.execute(graphQL, params);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Object executeFormPost(@RequestParam Map<String, String> queryParams,
                                GraphQLRequest graphQLRequest,
                                R request) {

        String idParam = queryParams.get("id");
        String queryParam = queryParams.get("query");
        String operationNameParam = queryParams.get("operationName");

        String id = Utils.isNotEmpty(idParam) ? idParam : graphQLRequest.getId();
        String query = Utils.isNotEmpty(queryParam) ? queryParam : graphQLRequest.getQuery();
        String operationName = Utils.isEmpty(operationNameParam) ? graphQLRequest.getOperationName() : operationNameParam;
        ExecutorParams<R> params = new ExecutorParams<>(new GraphQLRequest(id, query, operationName, graphQLRequest.getVariables()), request, TransportType.HTTP);

        return executor.execute(graphQL, params);
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            headers = { "Connection!=Upgrade", "Connection!=keep-alive, Upgrade" }
    )
    public Object executeGet(GraphQLRequest graphQLRequest, R request) {
        return get(graphQLRequest, request, TransportType.HTTP);
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE,
            headers = { "Connection!=Upgrade", "Connection!=keep-alive, Upgrade" }
    )
    public Object executeGetEventStream(GraphQLRequest graphQLRequest, R request) {
        return get(graphQLRequest, request, TransportType.HTTP_EVENT_STREAM);
    }

    private Object get(GraphQLRequest graphQLRequest, R request, TransportType transportType) {
        return executor.execute(graphQL, new ExecutorParams<>(graphQLRequest, request, transportType));
    }
}
