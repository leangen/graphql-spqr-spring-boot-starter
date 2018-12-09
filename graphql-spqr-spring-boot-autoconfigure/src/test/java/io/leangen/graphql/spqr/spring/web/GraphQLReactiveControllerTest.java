package io.leangen.graphql.spqr.spring.web;

import io.leangen.graphql.spqr.spring.autoconfigure.SpqrAutoConfiguration;
import io.leangen.graphql.spqr.spring.autoconfigure.SpqrReactiveAutoConfiguration;
import io.leangen.graphql.spqr.spring.test.ResolverBuilder_TestConfig;
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
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebFluxTest
@ContextConfiguration(classes = {SpqrAutoConfiguration.class, SpqrReactiveAutoConfiguration.class,
        ResolverBuilder_TestConfig.class})
@TestPropertySource(locations = "classpath:application.properties")
public class GraphQLReactiveControllerTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    public GraphQLController GraphQLController;

    private WebTestClient webTestClient = WebTestClient.bindToApplicationContext(context).build();

    @Value("${graphql.spqr.http.endpoint}")
    private String apiContext;


    @Test
    public void defaultControllerTest_GET_integer() throws Exception {
        FluxExchangeResult<Integer> result = webTestClient.post().uri(URI.create("/" + apiContext))
                .attribute("query", "{greetingFromBeanSource_integer}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange().returnResult(Integer.class);

        Integer resultString = result.getResponseBody().blockFirst();

        assertTrue(resultString==1984);

    }
}
