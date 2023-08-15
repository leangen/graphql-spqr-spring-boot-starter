package io.leangen.graphql.spqr.spring.web.reactive;

import java.net.URI;

import io.leangen.graphql.spqr.spring.autoconfigure.BaseAutoConfiguration;
import io.leangen.graphql.spqr.spring.autoconfigure.ReactiveAutoConfiguration;
import io.leangen.graphql.spqr.spring.test.ResolverBuilder_TestReactiveConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@WebFluxTest
@ContextConfiguration(classes = {
        BaseAutoConfiguration.class, ReactiveAutoConfiguration.class, ResolverBuilder_TestReactiveConfig.class
})
@TestPropertySource(locations = "classpath:application.properties")
public class GraphQLReactiveControllerTest {

    @Autowired
    private ApplicationContext context;

    private WebTestClient webTestClient;

    @Value("${graphql.spqr.http.endpoint}")
    private String apiContext;

    @Before
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    public void defaultControllerTest_POST_formUrlEncoded_mono() {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("query", "{greetingFromAnnotatedSourceReactive_mono}");

        webTestClient.post().uri(URI.create("/" + apiContext))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(c -> assertThat("", c.getResponseBody(), containsString("Hello world !")));
    }

    @Test
    public void defaultControllerTest_POST_flux() {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("query", "{greetingFromAnnotatedSourceReactive_flux}");

        webTestClient.post().uri(URI.create("/" + apiContext))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(c -> {
                    assertThat("", c.getResponseBody(), containsString("First Hello world !"));
                    assertThat("", c.getResponseBody(), containsString("Second Hello world !"));
                });
    }

    @Test
    public void defaultControllerTest_GET_with_variables() {
        webTestClient.get().uri("/" + apiContext + "?query={query}&variables={variables}",
                        "query Echo($contentInput: String){ echo(content: $contentInput)}",
                        "{\"contentInput\": \"Hello world\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(c -> assertThat("", c.getResponseBody(), containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_POST_with_variables() {
        webTestClient.post().uri("/" + apiContext + "?query={query}&variables={variables}",
                        "query Echo($contentInput: String){ echo(content: $contentInput)}",
                        "{\"contentInput\": \"Hello world1\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("{\"query\":\"{INVALID_QUERY}\",\"variables\":{\"contentInput\": \"Hello world2\"},\"operationName\":null}"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(c -> assertThat("", c.getResponseBody(), containsString("Hello world1")));
    }
}
