package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class ErrorMessage extends PayloadMessage<List<Map<String, ?>>> {

    @JsonCreator
    public ErrorMessage(@JsonProperty("id") String id, @JsonProperty("payload") List<Map<String, ?>> errors) {
        super(id, GQL_ERROR, errors);
    }
}
