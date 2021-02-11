package io.leangen.graphql.spqr.spring.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import graphql.GraphQL;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.generator.mapping.ConverterRegistry;
import io.leangen.graphql.metadata.strategy.type.DefaultTypeInfoGenerator;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import io.leangen.graphql.util.Defaults;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
public abstract class GraphQLController<R> {

    protected final GraphQL graphQL;
    protected final GraphQLExecutor<R> executor;
    private final ValueMapper valueMapper;

    public GraphQLController(GraphQL graphQL, GraphQLExecutor<R> executor) {
        this.graphQL = graphQL;
        this.executor = executor;
        this.valueMapper = Defaults.valueMapperFactory(new DefaultTypeInfoGenerator()).getValueMapper(
                Collections.emptyMap(),
                new GlobalEnvironment(null, null, null, new ConverterRegistry(Collections.emptyList(), Collections.emptyList()), null, null, null, null)
        );
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Object executeJsonPost(@RequestBody GraphQLRequest requestBody,
                                  @RequestParam(value = "query", required = false) String requestQuery,
                                  @RequestParam(value = "operationName", required = false) String requestOperationName,
                                  @RequestParam(value = "variables", required = false) String variablesJsonString,
                                  R request) throws IOException {
        String query = requestQuery == null ? requestBody.getQuery() : requestQuery;
        String operationName = requestOperationName == null ? requestBody.getOperationName() : requestOperationName;
        Map<String, Object> variables = parseJsonString(variablesJsonString);
        return executor.execute(graphQL, new GraphQLRequest(query, operationName, variables), request);
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {"application/graphql", "application/graphql;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Object executeGraphQLPost(@RequestBody String queryBody,
                                     GraphQLRequest graphQLRequest,
                                     R request) {
        String query = graphQLRequest.getQuery() == null ? queryBody : graphQLRequest.getQuery();
        return executor.execute(graphQL, new GraphQLRequest(query, graphQLRequest.getOperationName(), graphQLRequest.getVariables()), request);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Object executeFormPost(@RequestParam Map<String, String> queryParams,
                                  GraphQLRequest graphQLRequest,
                                  R request) {
        String queryParam = queryParams.get("query");
        String operationNameParam = queryParams.get("operationName");

        String query = StringUtils.isEmpty(queryParam) ? graphQLRequest.getQuery() : queryParam;
        String operationName = StringUtils.isEmpty(operationNameParam) ? graphQLRequest.getOperationName() : operationNameParam;

        return executor.execute(graphQL, new GraphQLRequest(query, operationName, graphQLRequest.getVariables()), request);
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            headers = { "Connection!=Upgrade", "Connection!=keep-alive, Upgrade" }
    )
    @ResponseBody
    public Object executeGet(@RequestParam(value = "query") String requestQuery,
                             @RequestParam(value = "operationName", required = false) String requestOperationName,
                             @RequestParam(value = "variables", required = false) String variablesJsonString,
                             R request) {
        Map<String, Object> variables = parseJsonString(variablesJsonString);
        return executor.execute(graphQL, new GraphQLRequest(requestQuery, requestOperationName, variables), request);
    }

    private Map<String, Object> parseJsonString(String json) {
        return valueMapper.fromString(json, GenericTypeReflector.annotate(Map.class));
    }
}
