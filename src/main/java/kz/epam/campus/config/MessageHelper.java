package kz.epam.campus.config;

import org.springframework.context.MessageSource;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class MessageHelper {

    private static final String SESSION_LOCALE_KEY = "appLocale";

    public static Locale resolveLocale(HttpServletRequest request) {

        String langParam = request.getParameter("lang");
        HttpSession session = request.getSession();

        if (langParam != null && !langParam.trim().isEmpty()) {
            Locale newLocale = Locale.forLanguageTag(langParam.trim());
            session.setAttribute(SESSION_LOCALE_KEY, newLocale);
            return newLocale;
        }

        Locale sessionLocale = (Locale) session.getAttribute(SESSION_LOCALE_KEY);
        if (sessionLocale != null) {
            return sessionLocale;
        }

        return Locale.ENGLISH;
    }

    public static String langQueryPrefix(HttpServletRequest request) {
        String qs = request.getQueryString();
        if (qs == null || qs.isEmpty()) return "?";
        String stripped = Arrays.stream(qs.split("&"))
                .filter(p -> !p.startsWith("lang="))
                .collect(Collectors.joining("&"));
        return stripped.isEmpty() ? "?" : "?" + stripped + "&";
    }

    public static String msg(ServletContext servletContext, Locale locale, String key) {
        MessageSource messageSource = WebApplicationContextUtils
                .getWebApplicationContext(servletContext)
                .getBean(MessageSource.class);
        return messageSource.getMessage(key, null, locale);
    }
}
