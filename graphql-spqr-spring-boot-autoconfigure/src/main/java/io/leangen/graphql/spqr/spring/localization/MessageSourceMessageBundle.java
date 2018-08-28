package io.leangen.graphql.spqr.spring.localization;

import io.leangen.graphql.metadata.messages.MessageBundle;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class MessageSourceMessageBundle implements MessageBundle {

    private final MessageSource messageSource;

    private final Locale schemaLocale;

    public MessageSourceMessageBundle(MessageSource messageSource, Locale schemaLocale) {
        this.schemaLocale = schemaLocale;
        this.messageSource = messageSource;
    }

    public MessageSourceMessageBundle(MessageSource messageSource) {
        this(messageSource, new Locale("en", "US"));
    }

    @Override
    public String getMessage(String key) {
        return this.messageSource.getMessage(key, null, schemaLocale);
    }

}
