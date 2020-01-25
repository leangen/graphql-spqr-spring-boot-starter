package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLType(name = "Sort")
public class Sorting {

    @GraphQLQuery
    public @GraphQLNonNull List<@GraphQLNonNull Order> orders;

    public Sorting() {
    }

    Sorting(Sort sort) {
        this.orders = sort.stream().map(Order::new).collect(Collectors.toList());
    }

    Sort toSort() {
        return Sort.by(orders.stream().map(Order::toOrder).collect(Collectors.toList()));
    }
}
