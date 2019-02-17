package io.leangen.graphql.spqr.spring.web;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public abstract class GraphQLController<REQ, RESP> {

    protected final GraphQL graphQL;
    protected final GraphQLExecutor<REQ, RESP> executor;

    public GraphQLController(GraphQL graphQL, GraphQLExecutor<REQ, RESP> executor) {
        this.graphQL = graphQL;
        this.executor = executor;
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public RESP executeJsonPost(@RequestBody GraphQLRequest requestBody,
                                GraphQLRequest requestParams,
                                REQ request) {
        String query = requestParams.getQuery() == null ? requestBody.getQuery() : requestParams.getQuery();
        String operationName = requestParams.getOperationName() == null ? requestBody.getOperationName() : requestParams.getOperationName();
        Map<String, Object> variables = requestParams.getVariables() == null ? requestBody.getVariables() : requestParams.getVariables();

        return executor.execute(graphQL, new GraphQLRequest(query, operationName, variables), request);
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {"application/graphql", "application/graphql;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public RESP executeGraphQLPost(@RequestBody String queryBody,
                                   GraphQLRequest graphQLRequest,
                                   REQ request) {
        String query = graphQLRequest.getQuery() == null ? queryBody : graphQLRequest.getQuery();
        return executor.execute(graphQL, new GraphQLRequest(query, graphQLRequest.getOperationName(), graphQLRequest.getVariables()), request);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public RESP executeFormPost(@RequestParam Map<String, String> queryParams,
                                GraphQLRequest graphQLRequest,
                                REQ request) {
        String queryParam = queryParams.get("query");
        String operationNameParam = queryParams.get("operationName");

        String query = StringUtils.isEmpty(queryParam) ? graphQLRequest.getQuery() : queryParam;
        String operationName = StringUtils.isEmpty(operationNameParam) ? graphQLRequest.getOperationName() : operationNameParam;

        return executor.execute(graphQL, new GraphQLRequest(query, operationName, graphQLRequest.getVariables()), request);
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            headers = "Connection!=Upgrade"
    )
    @ResponseBody
    public RESP executeGet(GraphQLRequest graphQLRequest, REQ request) {
        return executor.execute(graphQL, graphQLRequest, request);
    }
}
