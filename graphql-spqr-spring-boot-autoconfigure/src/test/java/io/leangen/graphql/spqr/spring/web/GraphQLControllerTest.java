package io.leangen.graphql.spqr.spring.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.leangen.graphql.spqr.spring.autoconfigure.BaseAutoConfiguration;
import io.leangen.graphql.spqr.spring.autoconfigure.MvcAutoConfiguration;
import io.leangen.graphql.spqr.spring.autoconfigure.SpringDataAutoConfiguration;
import io.leangen.graphql.spqr.spring.test.ResolverBuilder_TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {BaseAutoConfiguration.class, MvcAutoConfiguration.class,
        SpringDataAutoConfiguration.class, ResolverBuilder_TestConfig.class})
@TestPropertySource(locations = "classpath:application.properties")
public class GraphQLControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${graphql.spqr.http.endpoint}")
    private String apiContext;

    @Test
    public void defaultControllerTest_POST_applicationGraphql_noQueryParams() throws Exception {
        mockMvc.perform(
                    post("/"+apiContext)
                    .contentType("application/graphql")
                    .content("{greetingFromBeanSource_wiredAsComponent_byAnnotation}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_POST_applicationJson_noQueryParams() throws Exception {
        mockMvc.perform(
                post("/"+apiContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{greetingFromBeanSource_wiredAsComponent_byAnnotation}\",\"variables\":null,\"operationName\":null}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_POST_formUrlEncoded_noQueryParams() throws Exception {
        mockMvc.perform(
                post("/"+apiContext)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content("query="+ URLEncoder.encode("{greetingFromBeanSource_wiredAsComponent_byAnnotation}", StandardCharsets.UTF_8.toString())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_GET() throws Exception {
        mockMvc.perform(
                get("/"+apiContext)
                        .param("query","{greetingFromBeanSource_wiredAsComponent_byAnnotation}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_POST_applicationGraphql_INVALID() throws Exception {
        mockMvc.perform(
                post("/"+apiContext)
                        .contentType("application/graphql")
                        .content("{INVALID_QUERY}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FieldUndefined: Field 'INVALID_QUERY'")));
    }

    @Test
    public void defaultControllerTest_POST_applicationGraphql_overridingQueryParams() throws Exception {
        mockMvc.perform(
                post("/"+apiContext)
                        .param("query","{greetingFromBeanSource_wiredAsComponent_byAnnotation}")
                        .contentType("application/graphql")
                        .content("{INVALID_QUERY}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_POST_applicationJson_INVALID() throws Exception {
        mockMvc.perform(
                post("/"+apiContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{INVALID_QUERY}\",\"variables\":null,\"operationName\":null}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FieldUndefined: Field 'INVALID_QUERY'")));
    }

    @Test
    public void defaultControllerTest_POST_applicationJson_overridingQueryParams() throws Exception {
        mockMvc.perform(
                post("/" + apiContext)
                        .param("query", "{greetingFromBeanSource_wiredAsComponent_byAnnotation}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{INVALID_QUERY}\",\"variables\":null,\"operationName\":null}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_GET_with_variables() throws Exception {
        mockMvc.perform(
                get("/" + apiContext)
                        .param("query", "query Echo($contentInput: String){ echo(content: $contentInput)}")
                        .param("variables", "{\"contentInput\": \"Hello world\"}")
        )
                .andExpect(status().isOk())
                .andExpect(content().json("{\"data\":{\"echo\":\"Hello world\"}}", true));
    }

    @Test
    public void defaultControllerTest_POST_with_variables() throws Exception {
        mockMvc.perform(
                post("/" + apiContext)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{\"query\":\"{INVALID_QUERY}\",\"variables\":{\"contentInput\": \"Hello world2\"},\"operationName\":null}")
                        .param("query", "query Echo($contentInput: String){ echo(content: $contentInput)}")
                        .param("variables", "{\"contentInput\": \"Hello world1\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"data\":{\"echo\":\"Hello world1\"}}", true));
    }

    @Test
    public void defaultControllerTest_POST_formUrlEncoded_INVALID() throws Exception {
        mockMvc.perform(
                post("/"+apiContext)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content("query="+ URLEncoder.encode("{INVALID_QUERY}", StandardCharsets.UTF_8.toString())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FieldUndefined: Field 'INVALID_QUERY'")));
    }

    @Test
    public void defaultControllerTest_POST_formUrlEncoded_overridingQueryParams() throws Exception {
        mockMvc.perform(
                post("/"+apiContext)
                        .param("query","{greetingFromBeanSource_wiredAsComponent_byAnnotation}")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content("query="+ URLEncoder.encode("{INVALID_QUERY}", StandardCharsets.UTF_8.toString())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    public void defaultControllerTest_POST_spring_page() throws Exception {

        String withPage ="{\n" +
                "    springPageComponent_users(first:3,after:\"2id\") {\n" +
                "        pageInfo {\n" +
                "            startCursor\n" +
                "            endCursor\n" +
                "            hasNextPage\n" +
                "        }\n" +
                "        edges {\n" +
                "            node {\n" +
                "                id\n" +
                "                name\n" +
                "                age\n" +
                "                springPageComponent_user_projects(first:2,after:\"4\") {\n" +
                "                   pageInfo {\n" +
                "                       startCursor\n" +
                "                       endCursor\n" +
                "                       hasNextPage\n" +
                "                   }\n" +
                "                   edges {\n" +
                "                       node {\n" +
                "                           name\n" +
                "                       }\n" +
                "                   }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        mockMvc.perform(
                get("/"+apiContext)
                        .param("query",withPage))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToCompressingWhiteSpace("{" +
                        "\"data\":{\"springPageComponent_users\":{\"pageInfo\":{\"startCursor\":\"1\",\"endCursor\":\"3\",\"hasNextPage\":true}," +
                        "\"edges\":[{\"node\":{\"id\":\"0id\",\"name\":\"Duncan Idaho0\",\"age\":10," +
                        "\"springPageComponent_user_projects\":{\"pageInfo\":{\"startCursor\":\"1\",\"endCursor\":\"2\",\"hasNextPage\":true}," +
                        "\"edges\":[{\"node\":{\"name\":\"Project0\"}},{\"node\":{\"name\":\"Project1\"}}]}}}," +
                        "{\"node\":{\"id\":\"1id\",\"name\":\"Duncan Idaho1\",\"age\":11," +
                        "\"springPageComponent_user_projects\":{\"pageInfo\":{\"startCursor\":\"1\",\"endCursor\":\"2\",\"hasNextPage\":true}," +
                        "\"edges\":[{\"node\":{\"name\":\"Project0\"}},{\"node\":{\"name\":\"Project1\"}}]}}}," +
                        "{\"node\":{\"id\":\"2id\",\"name\":\"Duncan Idaho2\",\"age\":12," +
                        "\"springPageComponent_user_projects\":{\"pageInfo\":{\"startCursor\":\"1\",\"endCursor\":\"2\",\"hasNextPage\":true}," +
                        "\"edges\":[{\"node\":{\"name\":\"Project0\"}},{\"node\":{\"name\":\"Project1\"}}]}}}]}}}")));
    }
}
