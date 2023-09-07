package com.yaz.cm.vertx;

import com.yaz.cm.vertx.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class CustomMessageResolver extends AbstractMessageResolver {
    private Map<String, Properties> localizedMessages;

    public CustomMessageResolver() {
        super();
        readMessageBundles();
    }

    private void readMessageBundles() {
        localizedMessages = new LinkedHashMap<>();
        try {
            URL resourceUrl = this.getClass().getClassLoader().getResource(Constants.BUNDLES_DIR);
            if (resourceUrl == null) {
                log.error("Unable to access the default bundle directory: " + Constants.BUNDLES_DIR);
                return;
            }

            try (final var stream = Files.list(Paths.get(resourceUrl.toURI()))) {

                stream.filter(f -> f.toString().contains(".properties"))
                        .filter(f -> f.toString().contains("messages_"))
                        .forEach(this::readResourceBundle);

            }

            Properties properties = new Properties();
            InputStream iss = this.getClass().getClassLoader().getResourceAsStream
                    (Constants.DEFAULT_BUNDLE_FILE);
            if (iss == null) {
                log.warn("Unable to find the default resource bundle: " + Constants.DEFAULT_BUNDLE_FILE);
            } else {
                properties.load(iss);
                localizedMessages.put(Constants.DEFAULT_BUNDLE_KEY, properties);
                log.info("Loaded " + Constants.DEFAULT_BUNDLE_FILE + " resource bundle");
            }
        } catch (IOException e) {
            log.error("IOException occurred:", e);
        } catch (URISyntaxException e) {
            log.error("URISyntaxException occurred:", e);
        }
    }

    private void readResourceBundle(Path f) {
        try {
            if (!f.toFile().exists()) {
                log.error("File " + f.toFile().getAbsolutePath() + " does not exist");
                return;
            }
            Properties properties = new Properties();
            String languageCountry = f.getFileName()
                    .toString()
                    .substring(f.getFileName().toString().indexOf('_') + 1, f.getFileName()
                            .toString().indexOf('.'));
            if (f.toFile().exists()) {
                InputStream iss = this.getClass().getClassLoader().getResourceAsStream
                        (Constants.BUNDLES_DIR + "/" + f.toFile().getName());
                if (iss != null) {
                    properties.load(iss);
                    localizedMessages.put(languageCountry, properties);
                }
            }
            log.info("Loaded " + f.getFileName().toString() + " resource bundle");
        } catch (IOException e) {
            log.error("IOException occurred:", e);
        }
    }

    @Override
    public String resolveMessage(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
        Validate.notNull(context, "Context cannot be null");
        Validate.notNull(context.getLocale(), "Locale in context cannot be null");
        Validate.notNull(key, "Message key cannot be null");

        String languageContry = StringUtils.isEmptyOrWhitespace(context.getLocale().getCountry()
        ) ? context.getLocale().getLanguage() : context.getLocale().getLanguage() + "_" + context.getLocale()
                .getCountry();

        //get into account the {language} and {language}_{country} (de and de_DE) type of
        // localized resource bundles
        Properties messages = localizedMessages.entrySet()
                .stream()
                .filter(e -> e.getKey().contains(languageContry))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseGet(() -> localizedMessages.get(Constants.DEFAULT_BUNDLE_KEY));

        if (messages != null && messages.get(key) != null)
            return messages.get(key).toString();
        return null;
    }

    @Override
    public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
        Validate.notNull(key, "Message key cannot be null");
        if (context.getLocale() != null) {
            return "??" + key + "_" + context.getLocale().toString() + "??";
        }
        return "??" + key + "_" + "??";
    }
}