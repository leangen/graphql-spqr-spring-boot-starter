package io.leangen.graphql.spqr.spring.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class DefaultGraphiQLController {

    @Value("${graphiql.endpoint:#{null}}")
    private String graphqlEndpoint;

    @Value("${graphiql.websocket-endpoint:#{null}}")
    private String webSocketEndpoint;

    @Value("${graphql.spqr.websocket.mapping:#{null}}")
    private String webSocketDefaultEndpoint;

    @Value("${graphql.spqr.default-endpoint.mapping:/graphql}")
    private String graphqlDefaultEndpoint;

    @Value("${graphql.spqr.websocket.enabled:true}")
    private boolean webSocketEnabled;

    @Value("${graphiql.pageTitle:GraphiQL}")
    private String pageTitle;

    @ResponseBody
    @RequestMapping(value = "${graphiql.mapping:/graphiql}", produces = "text/html; charset=utf-8")
    public String graphiql() throws IOException {
        String template = StreamUtils.copyToString(new ClassPathResource("graphiql.html").getInputStream(), StandardCharsets.UTF_8);
        String graphQLEndpointUrl = graphqlEndpoint != null ? graphqlEndpoint : graphqlDefaultEndpoint;
        String webSocketEndpointUrl = webSocketEndpoint != null ? webSocketEndpoint : (webSocketDefaultEndpoint != null ? webSocketDefaultEndpoint : graphqlDefaultEndpoint);
        return template
                .replace("${pageTitle}", pageTitle)
                .replace("${graphQLEndpoint}", graphQLEndpointUrl)
                .replace("${webSocketEndpoint}", webSocketEnabled ? webSocketEndpointUrl : "");
    }
}
