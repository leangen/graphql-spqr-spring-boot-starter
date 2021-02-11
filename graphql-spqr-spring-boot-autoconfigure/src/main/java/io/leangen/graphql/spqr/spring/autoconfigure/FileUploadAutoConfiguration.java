package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.module.Module;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileUploadAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.multipart-upload.enabled", havingValue = "true")
    public Internal<Module> uploadModule() {
        FileUploadHandler uploadAdapter = new FileUploadHandler();
        return new Internal<>(context -> context.getSchemaGenerator()
                .withArgumentInjectors(uploadAdapter)
                .withTypeMappers(uploadAdapter)
        );
    }
}
