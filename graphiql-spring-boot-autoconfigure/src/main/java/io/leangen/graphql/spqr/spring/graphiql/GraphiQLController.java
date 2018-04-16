package io.leangen.graphql.spqr.spring.graphiql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class GraphiQLController {

    @Value("${graphiql.endpoint:#{null}}")
    private String graphqlEndpoint;

    @Value("${graphql.spqr.default-endpoint.mapping:graphql}")
    private String graphqlDefaultEndpoint;

    @Value("${graphiql.pageTitle:GraphiQL}")
    private String pageTitle;

    @ResponseBody
    @RequestMapping(value = "${graphiql.mapping:/graphiql}", produces = "text/html; charset=utf-8")
    public String graphiql() throws IOException {
        String template = StreamUtils.copyToString(new ClassPathResource("graphiql.html").getInputStream(), StandardCharsets.UTF_8);
        String endpointUrl = graphqlEndpoint == null ? graphqlDefaultEndpoint : graphqlEndpoint;
        endpointUrl = endpointUrl.startsWith("/") ? endpointUrl : "/" + endpointUrl;
        return template
                .replace("${pageTitle}", pageTitle)
                .replace("${graphqlEndpoint}", endpointUrl);
    }

    @ResponseBody
    @RequestMapping(value = "/client", produces = "text/html; charset=utf-8")
    public String subscriptions() throws IOException {
        return StreamUtils.copyToString(new ClassPathResource("subscription.html").getInputStream(), StandardCharsets.UTF_8);
    }
}
