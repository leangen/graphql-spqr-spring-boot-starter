package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@GraphQLType(name = "Pageable")
public class Pagination {

    @GraphQLQuery
    public Integer pageNumber;
    @GraphQLQuery
    public Integer pageSize;
    @GraphQLQuery
    public Sorting sort;

    public Pagination() {
    }

    Pagination(Pageable pageable) {
        this.pageNumber = pageable.getPageNumber();
        this.pageSize = pageable.getPageSize();
        this.sort = new Sorting(pageable.getSort());
    }

    Pageable toPageable() {
        return PageRequest.of(pageNumber != null ? pageNumber : 0, pageSize != null ? pageSize : 10, sort != null ? sort.toSort() : Sort.unsorted());
    }
}
