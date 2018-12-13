package io.leangen.graphql.spqr.spring.autoconfigure;

import java.util.Objects;

//Used only to wrap the internal beans, effectively hiding them from the user's application
//A better option would be to use @Bean(autowireCandidate=false) but that's only available in the latest versions of Spring
class Internal<T> {

    private final T item;

    Internal(T item) {
        this.item = Objects.requireNonNull(item);
    }

    T get() {
        return item;
    }
}
