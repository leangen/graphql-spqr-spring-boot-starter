package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class InitMessage extends PayloadMessage<Map<String, Object>> {

    @JsonCreator
    public InitMessage(@JsonProperty("id") String id, @JsonProperty("type") String type, @JsonProperty("payload") Map<String, Object> payload) {
        super(id, GQL_CONNECTION_INIT, payload);
    }
}
