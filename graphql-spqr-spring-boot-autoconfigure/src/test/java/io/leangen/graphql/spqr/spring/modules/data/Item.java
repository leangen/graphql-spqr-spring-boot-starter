package io.leangen.graphql.spqr.spring.modules.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

public class Item implements Persistable<Long> {
  @Id
  @GraphQLQuery(description = "Item identifier")
  Long id;
  @GraphQLQuery(description = "Item name")
  String name;

  @Override
  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  @JsonIgnore
  @GraphQLIgnore
  public boolean isNew() {
    return id == null;
  }
}
