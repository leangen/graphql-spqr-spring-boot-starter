package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.annotations.WithResolverBuilder;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GraphQLApi
@WithResolverBuilder(SpringDataRepositoryResolverBuilder.class)
public interface ItemRepository extends R2dbcRepository<Item, Long> {
  Flux<Item> findAllByName(String name);

  @Modifying
  @Query("UPDATE item SET name = :name where id = :id")
  Mono<Integer> updateNameById(String name, Long id);

}
