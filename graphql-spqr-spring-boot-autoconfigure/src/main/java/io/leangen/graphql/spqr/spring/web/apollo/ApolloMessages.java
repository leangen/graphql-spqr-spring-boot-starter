package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.annotation.JsonInclude;
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

import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_COMPLETE;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_CONNECTION_ACK;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_CONNECTION_KEEP_ALIVE;

public class ApolloMessages {

    private static final ApolloMessage CONNECTION_ACK = new ApolloMessage(GQL_CONNECTION_ACK);
    private static final ApolloMessage KEEP_ALIVE = new ApolloMessage(GQL_CONNECTION_KEEP_ALIVE);

    private static final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

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
