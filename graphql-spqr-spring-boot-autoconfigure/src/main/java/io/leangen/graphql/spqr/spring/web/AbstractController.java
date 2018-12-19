package io.leangen.graphql.spqr.spring.web;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

public abstract class AbstractController<T> {
    protected final GraphQL graphQL;

    public AbstractController(GraphQL graphQL) {
        this.graphQL = graphQL;
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public Map<String, Object> executeJsonPost(@RequestBody GraphQLRequest requestBody,
                                               GraphQLRequest requestParams,
                                               T raw) {
        String query = requestParams.getQuery() == null ? requestBody.getQuery() : requestParams.getQuery();
        String operationName = requestParams.getOperationName() == null ? requestBody.getOperationName() : requestParams.getOperationName();
        Map<String, Object> variables = requestParams.getVariables() == null ? requestBody.getVariables() : requestParams.getVariables();

        ExecutionResult executionResult = graphQL.execute(
                input(new GraphQLRequest(query, operationName, variables), raw));
        return executionResult.toSpecification();
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {"application/graphql", "application/graphql;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public Map<String, Object> executeGraphQLPost(@RequestBody String queryBody,
                                                  GraphQLRequest request,
                                                  T raw) {
        String query = request.getQuery() == null ? queryBody : request.getQuery();

        ExecutionResult executionResult = graphQL.execute(
                input(new GraphQLRequest(query, request.getOperationName(), request.getVariables()), raw));
        return executionResult.toSpecification();
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public Map<String, Object> executeFormPost(@RequestParam Map<String, String> queryParams,
                                               GraphQLRequest request,
                                               T raw) {
        String queryParam = queryParams.get("query");
        String operationNameParam = queryParams.get("operationName");

        String query = StringUtils.isEmpty(queryParam) ? request.getQuery() : queryParam;
        String operationName = StringUtils.isEmpty(operationNameParam) ? request.getOperationName() : operationNameParam;

        ExecutionResult executionResult = graphQL.execute(
                input(new GraphQLRequest(query, operationName, request.getVariables()), raw));
        return executionResult.toSpecification();
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            headers = "Connection!=Upgrade"
    )
    @ResponseBody
    public Map<String, Object> executeGet(GraphQLRequest request,
                                          T raw) {

        ExecutionResult executionResult = graphQL.execute(
                input(request, raw));
        return executionResult.toSpecification();
    }

    protected abstract ExecutionInput input(GraphQLRequest request, T raw);
}
