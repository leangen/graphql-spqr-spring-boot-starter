package io.leangen.graphql.spqr.spring.localization;

import io.leangen.graphql.metadata.messages.MessageBundle;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class SpringSpqrMessageBundle implements MessageBundle {

    private final MessageSource messageSource;

    private final Locale schemaLocale;

    public SpringSpqrMessageBundle(MessageSource messageSource, Locale schemaLocale) {
        this.schemaLocale = schemaLocale;
        this.messageSource = messageSource;
    }

    public SpringSpqrMessageBundle(MessageSource messageSource) {
        this.schemaLocale = new Locale("en", "US");
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String key) {
        return this.messageSource.getMessage(key, null, schemaLocale);
    }

}
