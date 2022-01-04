package io.leangen.graphql.spqr.spring.modules.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:r2dbc.properties")
public class SpringDataRepositoryResolverBuilderTest {
  @LocalServerPort
  protected int port;
  @Value("${graphql.spqr.http.endpoint}")
  private String apiContext;
  @Autowired
  private ItemRepository repository;

  private WebTestClient webTestClient;

  private URI uri;

  @Before
  public void setUp() {
    webTestClient = WebTestClient.bindToServer().build();
    uri = URI.create("http://localhost:" + port + "/" + apiContext);
  }

  @After
  public void cleanUp() {
    repository.deleteAll().block(Duration.ofSeconds(1L));
  }

  @Test
  public void saveItem() {
    post("mutation", "saveItem", "entity: {id: null, name: \"saved item name\"}", "name",
      "{\"data\":{\"saveItem\":[{\"name\":\"saved item name\"}]}}");
  }

  @Test
  public void countItems() {
    post("query", "countItems", null, null, "{\"data\":{\"countItems\":[0]}}");
    saveNewItem("name");
    post("query", "countItems", null, null, "{\"data\":{\"countItems\":[1]}}");
  }

  @Test
  public void deleteItemById() {
    Item i = saveNewItem("a name");
    post("mutation", "deleteItemById", "id: " + i.getId(), null, "{\"data\":{\"deleteItemById\":[]}}");
    assertFalse(repository.findById(i.getId()).blockOptional(Duration.ofSeconds(1L)).isPresent());
  }

  @Test
  public void findItemById() {
    Item i = saveNewItem("a name");
    post("query", "findItemById", "id: " + i.getId(), "id name",
      "{\"data\":{\"findItemById\":[{\"id\":" + i.getId() + ",\"name\":\"a name\"}]}}");
  }

  @Test
  public void findAllItemsByName() {
    saveNewItem("best name");
    saveNewItem("best name");
    post("query", "findAllItemsByName", "name: \"best name\"", "name",
      "{\"data\":{\"findAllItemsByName\":[{\"name\":\"best name\"},{\"name\":\"best name\"}]}}");
  }

  @Test
  public void updateItemNameById() {
    Item i = saveNewItem("old name");
    post("mutation", "updateItemNameById", "id: " + i.getId() + ", name: \"new name\"", null,
      "{\"data\":{\"updateItemNameById\":[1]}}");
  }

  /**
   * All database to persist new item and update the generated ID.
   * 
   * @param name item name
   * @return item
   */
  private Item saveNewItem(String name) {
    Item i = new Item();
    i.setName(name);
    return repository.save(i).block(Duration.ofSeconds(1L));
  }

  /**
   * Test helper to send graphQL HTTP Post.
   * 
   * @param gqlType query or mutation
   * @param operation list available in documentation http://localhost:{port}/gui
   * @param inputData parameters
   * @param outputFields id and name are options
   * @param expectedResult response body
   */
  private void post(String gqlType, String operation, String inputData, String outputFields, String expectedResult) {
    webTestClient.post().uri(uri).contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters
        .fromValue(Collections.singletonMap("query", gqlType + " " + gqlSyntax(operation, inputData, outputFields))))
      .exchange().expectStatus().isOk().expectBody(String.class)
      .consumeWith(c -> assertThat("", c.getResponseBody(), containsString(expectedResult)));
  }

  private String gqlSyntax(String operation, String inputData, String outputFields) {
    return "{" + operation + (inputData == null ? "" : "(" + inputData + ")")
      + (outputFields == null ? "" : "{" + outputFields + "}") + "}";
  }

}
