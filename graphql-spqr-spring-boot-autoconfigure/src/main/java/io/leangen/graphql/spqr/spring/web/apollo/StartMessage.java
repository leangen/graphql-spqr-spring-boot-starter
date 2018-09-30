package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;

public class StartMessage extends PayloadMessage<GraphQLRequest> {

    @JsonCreator
    public StartMessage(@JsonProperty("id") String id, @JsonProperty("type") String type, @JsonProperty("payload") GraphQLRequest payload) {
        super(id, GQL_START, payload);
    }
}
