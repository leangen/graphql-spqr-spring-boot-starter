package io.leangen.graphql.spqr.spring.graphiql;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GraphiQLAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value = "graphiql.enabled", havingValue = "true", matchIfMissing = true)
    GraphiQLController graphiQLController() {
        return new GraphiQLController();
    }
}
