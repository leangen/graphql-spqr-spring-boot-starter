package io.leangen.graphql.spqr.spring.web;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public abstract class GraphQLController<R> {

    protected final GraphQL graphQL;
    protected final GraphQLExecutor<R> executor;

    public GraphQLController(GraphQL graphQL, GraphQLExecutor<R> executor) {
        this.graphQL = graphQL;
        this.executor = executor;
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = { MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_JSON_VALUE },
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<Object> executeJsonPost(@RequestBody GraphQLRequest requestBody,
            GraphQLRequest requestParams,
            R request) {
        String query = requestParams.getQuery() == null ? requestBody.getQuery() : requestParams.getQuery();
        Map<String, Object> variables = requestParams.getVariables() == null ? requestBody.getVariables() : requestParams.getVariables();
        String operationName =
                requestParams.getOperationName() == null ? requestBody.getOperationName() : requestParams.getOperationName();

        Object result = executor.execute(graphQL, new GraphQLRequest(query, operationName, variables), request);

        HttpHeaders headers = new HttpHeaders();
        CacheControlHeader.addCacheControlHeader(result, headers);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = { "application/graphql", "application/graphql;charset=UTF-8" },
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<Object> executeGraphQLPost(@RequestBody String queryBody,
            GraphQLRequest graphQLRequest,
            R request) {
        String query = graphQLRequest.getQuery() == null ? queryBody : graphQLRequest.getQuery();
        Object result = executor.execute(graphQL,
                new GraphQLRequest(query, graphQLRequest.getOperationName(), graphQLRequest.getVariables()), request);

        HttpHeaders headers = new HttpHeaders();
        CacheControlHeader.addCacheControlHeader(result, headers);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded;charset=UTF-8" },
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<Object> executeFormPost(@RequestParam Map<String, String> queryParams,
            GraphQLRequest graphQLRequest,
            R request) {
        String queryParam = queryParams.get("query");
        String operationNameParam = queryParams.get("operationName");

        String query = StringUtils.isEmpty(queryParam) ? graphQLRequest.getQuery() : queryParam;
        String operationName = StringUtils.isEmpty(operationNameParam) ? graphQLRequest.getOperationName() : operationNameParam;

        Object result = executor.execute(graphQL, new GraphQLRequest(query, operationName, graphQLRequest.getVariables()),
                request);

        HttpHeaders headers = new HttpHeaders();
        CacheControlHeader.addCacheControlHeader(result, headers);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            headers = "Connection!=Upgrade"
    )
    @ResponseBody
    public ResponseEntity<Object> executeGet(GraphQLRequest graphQLRequest, R request) {
        Object result = executor.execute(graphQL, graphQLRequest, request);

        HttpHeaders headers = new HttpHeaders();
        CacheControlHeader.addCacheControlHeader(result, headers);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

}
