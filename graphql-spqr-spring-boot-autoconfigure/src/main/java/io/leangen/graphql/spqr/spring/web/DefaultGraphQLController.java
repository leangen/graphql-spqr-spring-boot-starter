package io.leangen.graphql.spqr.spring.web;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class DefaultGraphQLController {

    private final GraphQL graphQL;

    @Autowired
    public DefaultGraphQLController(GraphQLSchema schema) {
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    @PostMapping(
            value = "/${graphql.spqr.default-endpoint.mapping:graphql}",
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

        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables)
                .context(raw)
                .build());
        return executionResult.toSpecification();
    }

    @PostMapping(
            value = "/${graphql.spqr.default-endpoint.mapping:graphql}",
            consumes = {"application/graphql", "application/graphql;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public Map<String, Object> executeGraphQLPost(@RequestBody String queryBody,
                                                  GraphQLRequest requestParams,
                                                  HttpServletRequest raw) {
        String query = requestParams.getQuery() == null ? queryBody : requestParams.getQuery();

        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(requestParams.getOperationName())
                .variables(requestParams.getVariables())
                .context(raw)
                .build());
        return executionResult.toSpecification();
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/${graphql.spqr.default-endpoint.mapping:graphql}",
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

        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(request.getVariables())
                .context(raw)
                .build());
        return executionResult.toSpecification();
    }

    @GetMapping(
            value = "/${graphql.spqr.default-endpoint.mapping:graphql}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public Map<String, Object> executeGet(GraphQLRequest request,
                                          HttpServletRequest raw) {
        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .operationName(request.getOperationName())
                .variables(request.getVariables())
                .context(raw)
                .build());
        return executionResult.toSpecification();
    }

}
