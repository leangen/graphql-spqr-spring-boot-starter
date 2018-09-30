package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class ConnectionErrorMessage extends PayloadMessage<Map<String, ?>> {

    @JsonCreator
    public ConnectionErrorMessage(@JsonProperty("payload") Map<String, ?> error) {
        super(null, GQL_CONNECTION_ERROR, error);
    }
}
