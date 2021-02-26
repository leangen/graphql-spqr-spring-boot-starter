package io.leangen.graphql.spqr.spring.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLRequest {

    private final String id;
    private final String query;
    private final String operationName;
    private final Map<String, Object> variables;

    @JsonCreator
    public GraphQLRequest(@JsonProperty("id") String id,
                          @JsonProperty("query") String query,
                          @JsonProperty("operationName") String operationName,
                          @JsonProperty("variables") Map<String, Object> variables) {
        this.id = id;
        this.query = query;
        this.operationName = operationName;
        this.variables = variables != null ? variables : Collections.emptyMap();
    }

    public String getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
