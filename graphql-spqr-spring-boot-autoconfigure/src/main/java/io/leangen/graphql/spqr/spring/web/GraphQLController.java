package io.leangen.graphql.spqr.spring.web;

import java.util.*;
import java.util.stream.Collectors;

import graphql.GraphQL;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.generator.mapping.ConverterRegistry;
import io.leangen.graphql.metadata.messages.EmptyMessageBundle;
import io.leangen.graphql.metadata.strategy.type.DefaultTypeInfoGenerator;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import io.leangen.graphql.util.Defaults;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                new GlobalEnvironment(EmptyMessageBundle.INSTANCE, null, null, new ConverterRegistry(Collections.emptyList(), Collections.emptyList()), null, null, null, null)
        );
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Object executeJsonPost(@RequestBody GraphQLRequest requestBody,
                                  GraphQLRequest requestParams,
                                  R request) {
        String query = requestParams.getQuery() == null ? requestBody.getQuery() : requestParams.getQuery();
        String operationName = requestParams.getOperationName() == null ? requestBody.getOperationName() : requestParams.getOperationName();
        Map<String, Object> variables = requestParams.getVariables().isEmpty() ? requestBody.getVariables() : requestParams.getVariables();

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
    public Object executeGet(GraphQLRequest graphQLRequest, R request) {
        return executor.execute(graphQL, graphQLRequest, request);
    }

    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public Object executeMultipartFileUpload(
            @RequestParam("operations") String requestString,
            @RequestParam("map") String mappingString,
            @RequestParam Map<String, MultipartFile> multipartFiles,
            R request) {
        GraphQLRequest graphQLRequest = valueMapper.fromString(requestString, GenericTypeReflector.annotate(GraphQLRequest.class));
        Map<String, List<String>> fileMappings = valueMapper.fromString(mappingString, GenericTypeReflector.annotate((Map.class)));

        Map<String, Object> values = new LinkedHashMap<>();
        fileMappings.forEach((fileKey, variables) -> {
            for (String variable : variables) {
                String[] parts = variable.split("\\.");
                String path = parts[0] + Arrays.stream(parts).skip(1).collect(Collectors.joining("][", "[", "]"));
                values.put(path, multipartFiles.get(fileKey));
            }
        });

        DataBinder binder = new DataBinder(graphQLRequest, "operations");
        binder.setIgnoreUnknownFields(false);
        binder.setIgnoreInvalidFields(false);
        binder.bind(new MutablePropertyValues(values));

        return executor.execute(graphQL, graphQLRequest, request);
    }
}
