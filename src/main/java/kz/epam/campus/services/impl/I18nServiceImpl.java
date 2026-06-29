package kz.epam.campus.services.impl;

import kz.epam.campus.services.I18nService;
import org.springframework.context.MessageSource;

import java.util.Locale;

//TODO add @Service annotation otherwise it won't be registered in AppContext

public class I18nServiceImpl implements I18nService {

    private final MessageSource messageSource;
    private Locale locale = Locale.ENGLISH;

    public I18nServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key) {
        return messageSource.getMessage(key, null, locale);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}