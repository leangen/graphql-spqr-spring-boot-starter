package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitMessage.class, name = ApolloMessage.GQL_CONNECTION_INIT),
        @JsonSubTypes.Type(value = ApolloMessage.class, name = ApolloMessage.GQL_CONNECTION_TERMINATE),
        @JsonSubTypes.Type(value = ApolloMessage.class, name = ApolloMessage.GQL_STOP),
        @JsonSubTypes.Type(value = StartMessage.class, name = ApolloMessage.GQL_START),
        @JsonSubTypes.Type(value = ConnectionErrorMessage.class, name = ApolloMessage.GQL_CONNECTION_ERROR),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = ApolloMessage.GQL_ERROR),
})
@SuppressWarnings("WeakerAccess")
public class ApolloMessage {

    private final String id;
    private final String type;

    //Client messages
    public static final String GQL_CONNECTION_INIT = "connection_init";
    public static final String GQL_CONNECTION_TERMINATE = "connection_terminate";
    public static final String GQL_START = "start";
    public static final String GQL_STOP = "stop";

    //Server messages
    public static final String GQL_CONNECTION_ACK = "connection_ack";
    public static final String GQL_CONNECTION_ERROR = "connection_error";
    public static final String GQL_CONNECTION_KEEP_ALIVE = "ka";
    public static final String GQL_DATA = "data";
    public static final String GQL_ERROR = "error";
    public static final String GQL_COMPLETE = "complete";

    ApolloMessage(String type) {
        this(null, type);
    }

    @JsonCreator
    public ApolloMessage(@JsonProperty("id") String id, @JsonProperty("type") String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
