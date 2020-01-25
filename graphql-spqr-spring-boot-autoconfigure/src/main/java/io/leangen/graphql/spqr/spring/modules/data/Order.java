package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import org.springframework.data.domain.Sort;

public class Order {

    @GraphQLQuery
    public Sort.Direction direction;
    @GraphQLQuery
    public @GraphQLNonNull String property;
    @GraphQLQuery
    public Sort.NullHandling nullHandlingHint;
    @GraphQLQuery
    public Boolean ignoreCase = false;

    public Order() {
    }

    Order(Sort.Order order) {
        this.direction = order.getDirection();
        this.property = order.getProperty();
        this.nullHandlingHint = order.getNullHandling();
        this.ignoreCase = order.isIgnoreCase();
    }

    Sort.Order toOrder() {
        Sort.Order order = new Sort.Order(direction, property, nullHandlingHint != null ? nullHandlingHint : Sort.NullHandling.NATIVE);
        return ignoreCase ? order.ignoreCase() : order;
    }
}
