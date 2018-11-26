package io.leangen.graphql.spqr.spring.web;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.DefaultGlobalContext;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@CrossOrigin
public class GraphQLController {

    private final GraphQL graphQL;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    @Autowired
    public GraphQLController(GraphQL graphQL, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.graphQL = graphQL;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public Map<String, Object> executeJsonPost(@RequestBody GraphQLRequest requestBody,
                                               GraphQLRequest requestParams,
                                               HttpServletRequest raw) {
        String query = requestParams.getQuery() == null ? requestBody.getQuery() : requestParams.getQuery();
        String operationName = requestParams.getOperationName() == null ? requestBody.getOperationName() : requestParams.getOperationName();
        Map<String, Object> variables = requestParams.getVariables() == null ? requestBody.getVariables() : requestParams.getVariables();

        ExecutionResult executionResult = graphQL.execute(
                input(query, operationName, variables, raw));
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
                                                  HttpServletRequest raw) {
        String query = request.getQuery() == null ? queryBody : request.getQuery();

        ExecutionResult executionResult = graphQL.execute(
                input(query, request.getOperationName(), request.getVariables(), raw));
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
                                               HttpServletRequest raw) {
        String queryParam = queryParams.get("query");
        String operationNameParam = queryParams.get("operationName");

        String query = StringUtils.isEmpty(queryParam) ? request.getQuery() : queryParam;
        String operationName = StringUtils.isEmpty(operationNameParam) ? request.getOperationName() : operationNameParam;

        ExecutionResult executionResult = graphQL.execute(
                input(query, operationName, request.getVariables(), raw));
        return executionResult.toSpecification();
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            headers = "Connection!=Upgrade"
    )
    @ResponseBody
    public Map<String, Object> executeGet(GraphQLRequest request,
                                          HttpServletRequest raw) {

        ExecutionResult executionResult = graphQL.execute(
                input(request.getQuery(), request.getOperationName(), request.getVariables(), raw));
        return executionResult.toSpecification();
    }

    private ExecutionInput input(String query, String operationName, Map<String, Object> variables, HttpServletRequest raw) {
        ExecutionInput.Builder inputBuilder = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables)
                .context(new DefaultGlobalContext(raw));
        if (dataLoaderRegistryFactory != null) {
            inputBuilder.dataLoaderRegistry(dataLoaderRegistryFactory.createDataLoaderRegistry());
        }
        return inputBuilder.build();
    }
}
