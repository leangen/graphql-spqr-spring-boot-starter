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
import io.leangen.graphql.spqr.spring.web.dto.ExecutorParams;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import io.leangen.graphql.spqr.spring.web.dto.TransportType;
import io.leangen.graphql.util.Defaults;
import io.leangen.graphql.util.Utils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.http.MediaType;
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
            headers = { "Connection!=Upgrade", "Connection!=keep-alive, Upgrade",
                        "Connection!=upgrade", "Connection!=keep-alive, upgrade"}
    )
    public Object executeGet(GraphQLRequest graphQLRequest, R request) {
        return get(graphQLRequest, request, TransportType.HTTP);
    }

    @GetMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE,
            headers = { "Connection!=Upgrade", "Connection!=keep-alive, Upgrade",
                        "Connection!=upgrade", "Connection!=keep-alive, upgrade"}
    )
    public Object executeGetEventStream(GraphQLRequest graphQLRequest, R request) {
        return get(graphQLRequest, request, TransportType.HTTP_EVENT_STREAM);
    }

    private Object get(GraphQLRequest graphQLRequest, R request, TransportType transportType) {
        return executor.execute(graphQL, new ExecutorParams<>(graphQLRequest, request, transportType));
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
            R request)
    {
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

        return executeGet(graphQLRequest, request);
    }
}
