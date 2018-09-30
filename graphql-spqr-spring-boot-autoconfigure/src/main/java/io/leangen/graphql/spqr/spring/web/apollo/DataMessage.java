package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.ExecutionResult;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class DataMessage extends PayloadMessage<Map<String, Object>> {

    @JsonCreator
    public DataMessage(@JsonProperty("id") String id, @JsonProperty("payload") ExecutionResult payload) {
        super(id, GQL_DATA, payload.toSpecification());
    }
}
