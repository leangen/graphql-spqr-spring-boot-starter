package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ErrorType;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final ApolloMessage CONNECTION_ACK = new ApolloMessage(GQL_CONNECTION_ACK);
    private static final ApolloMessage KEEP_ALIVE = new ApolloMessage(GQL_CONNECTION_KEEP_ALIVE);

    private static final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private ApolloMessage(String type) {
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

    public static ApolloMessage from(TextMessage message) throws IOException {
        return mapper.readValue(message.getPayload(), ApolloMessage.class);
    }

    public static TextMessage connectionAck() throws JsonProcessingException {
        return jsonMessage(CONNECTION_ACK);
    }

    public static TextMessage keepAlive() throws JsonProcessingException {
        return jsonMessage(KEEP_ALIVE);
    }

    public static TextMessage connectionError(final String message) throws JsonProcessingException {
        return jsonMessage(new ConnectionErrorMessage(Collections.singletonMap("message", message)));
    }

    public static TextMessage connectionError() throws JsonProcessingException {
        return connectionError("Invalid message");
    }

    public static TextMessage data(String id, ExecutionResult result) throws JsonProcessingException {
        return jsonMessage(new DataMessage(id, result));
    }

    public static TextMessage complete(String id) throws JsonProcessingException {
        return jsonMessage(new ApolloMessage(id, GQL_COMPLETE));
    }

    public static TextMessage error(String id, List<GraphQLError> errors) throws JsonProcessingException {
        return jsonMessage(new ErrorMessage(id, errors.stream()
                .filter(error -> !error.getErrorType().equals(ErrorType.DataFetchingException))
                .map(GraphQLError::toSpecification)
                .collect(Collectors.toList())));
    }

    public static TextMessage error(String id, Throwable exception) throws JsonProcessingException {
        return error(id, exception.getMessage());
    }

    public static TextMessage error(String id, String message) throws JsonProcessingException {
        return jsonMessage(new ErrorMessage(id, Collections.singletonList(Collections.singletonMap("message", message))));
    }

    private static TextMessage jsonMessage(ApolloMessage message) throws JsonProcessingException {
        return new TextMessage(mapper.writeValueAsString(message));
    }
}
