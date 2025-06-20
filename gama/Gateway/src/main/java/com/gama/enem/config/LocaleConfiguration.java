package com.gama.enem.config;

import java.time.Duration;
import java.util.Locale;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.i18n.LocaleContextResolver;

@Configuration
public class LocaleConfiguration {

    @Bean(name = "localeContextResolver")
    public LocaleContextResolver localeContextResolver() {
        return new AngularCookieLocaleContextResolver();
    }

    @Bean
    public WebFilter localeChangeFilter(LocaleContextResolver localeContextResolver) {
        return (exchange, chain) -> {
            // Find locale change in query param. Must also look form params ?
            String newLocale = exchange.getRequest().getQueryParams().getFirst("language");
            if (newLocale != null) {
                localeContextResolver.setLocaleContext(exchange, new SimpleLocaleContext(StringUtils.parseLocaleString(newLocale)));
            }
            // Proceed in any case.
            return chain.filter(exchange);
        };
    }

    static class AngularCookieLocaleContextResolver implements LocaleContextResolver {

        private static final String LOCALE_REQUEST_ATTRIBUTE_NAME = AngularCookieLocaleContextResolver.class.getName() + ".LOCALE";

        private static final String TIME_ZONE_REQUEST_ATTRIBUTE_NAME = AngularCookieLocaleContextResolver.class.getName() + ".TIME_ZONE";

        private static final String QUOTE = "%22";

        private static final String COOKIE_NAME = "NG_TRANSLATE_LANG_KEY";

        private static final String COOKIE_PATH = "/";

        protected final Log logger = LogFactory.getLog(getClass());

        @Override
        @Nonnull
        public LocaleContext resolveLocaleContext(@Nonnull ServerWebExchange exchange) {
            parseLocaleCookieIfNecessary(exchange);
            return new TimeZoneAwareLocaleContext() {
                @Override
                public Locale getLocale() {
                    return (Locale) exchange.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
                }

                @Override
                public TimeZone getTimeZone() {
                    return (TimeZone) exchange.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
                }
            };
        }

        @Override
        public void setLocaleContext(@Nonnull ServerWebExchange exchange, LocaleContext localeContext) {
            Assert.notNull(exchange.getResponse(), "ServerHttpResponse is required for AngularCookieLocaleContextResolver");

            Locale locale = null;
            TimeZone timeZone = null;
            if (localeContext != null) {
                locale = localeContext.getLocale();
                if (localeContext instanceof TimeZoneAwareLocaleContext) {
                    timeZone = ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
                }
                addCookie(
                    exchange.getResponse(),
                    QUOTE + (locale != null ? locale.toString() : "-") + (timeZone != null ? ' ' + timeZone.getID() : "") + QUOTE
                );
            } else {
                removeCookie(exchange.getResponse());
            }
            exchange
                .getAttributes()
                .put(LOCALE_REQUEST_ATTRIBUTE_NAME, (locale != null ? locale : LocaleContextHolder.getLocale(exchange.getLocaleContext())));
            if (timeZone != null) {
                exchange.getAttributes().put(TIME_ZONE_REQUEST_ATTRIBUTE_NAME, timeZone);
            } else {
                exchange.getAttributes().remove(TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
            }
        }

        private void addCookie(@Nonnull ServerHttpResponse response, String cookieValue) {
            Assert.notNull(response, "ServerHttpResponse must not be null");
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, cookieValue).path(COOKIE_PATH).build();
            response.addCookie(cookie);
            if (logger.isDebugEnabled()) {
                logger.debug("Added cookie with name [" + COOKIE_NAME + "] and value [" + cookieValue + "]");
            }
        }

        private void removeCookie(@Nonnull ServerHttpResponse response) {
            Assert.notNull(response, "ServerHttpResponse must not be null");
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "").path(COOKIE_PATH).maxAge(Duration.ZERO).build();
            response.addCookie(cookie);
            if (logger.isDebugEnabled()) {
                logger.debug("Removed cookie with name [" + COOKIE_NAME + "]");
            }
        }

        private void parseLocaleCookieIfNecessary(ServerWebExchange exchange) {
            if (exchange.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null) {
                // Retrieve and parse cookie value.
                HttpCookie cookie = exchange.getRequest().getCookies().getFirst(COOKIE_NAME);
                Locale locale = null;
                TimeZone timeZone = null;
                if (cookie != null) {
                    String value = cookie.getValue();

                    // Remove the double quote
                    value = StringUtils.replace(value, QUOTE, "");

                    String localePart = value;
                    String timeZonePart = null;
                    int spaceIndex = localePart.indexOf(' ');
                    if (spaceIndex != -1) {
                        localePart = value.substring(0, spaceIndex);
                        timeZonePart = value.substring(spaceIndex + 1);
                    }
                    locale = !"-".equals(localePart) ? StringUtils.parseLocaleString(localePart.replace('-', '_')) : null;
                    if (timeZonePart != null) {
                        timeZone = StringUtils.parseTimeZoneString(timeZonePart);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "Parsed cookie value [" +
                            cookie.getValue() +
                            "] into locale '" +
                            locale +
                            "'" +
                            (timeZone != null ? " and time zone '" + timeZone.getID() + "'" : "")
                        );
                    }
                }
                if (locale != null) {
                    exchange.getAttributes().put(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
                }
                if (timeZone != null) {
                    exchange.getAttributes().put(TIME_ZONE_REQUEST_ATTRIBUTE_NAME, timeZone);
                } else {
                    exchange.getAttributes().remove(TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
                }
            }
        }
    }
}
