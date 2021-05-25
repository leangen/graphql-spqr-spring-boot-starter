package io.leangen.graphql.spqr.spring.web.reactive;

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

import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void defaultControllerTest_GET_mono() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + apiContext)
                        .queryParam("query", "{query}")
                        .build("{greetingFromAnnotatedSourceReactive_mono}"))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(c -> assertThat("", c.getResponseBody(), containsString("Hello world !")));
    }

    @Test
    public void defaultControllerTest_GET_persistedQuery_mono() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + apiContext)
                        .queryParam("extensions", "{persisted}")
                        .build("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"4b758938f2d00323147290e3b0d041e6a0952e2c694ab2c0ea7212ca08f337b3\"}}"))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(c -> assertThat("", c.getResponseBody(), containsString("Invalid Syntax : offending token '<EOF>' at line 1 column 1\"")));
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
}
