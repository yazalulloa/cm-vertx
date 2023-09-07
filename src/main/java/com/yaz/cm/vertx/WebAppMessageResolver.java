package com.yaz.cm.vertx;

import com.yaz.cm.vertx.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class WebAppMessageResolver extends AbstractMessageResolver {
    private Map<String, Properties> localizedMessages;

    public WebAppMessageResolver() {
        super();
        readMessageBundles();
    }

    private void readMessageBundles() {
        localizedMessages = new LinkedHashMap<>();
        try {

            try (final var stream = Files.list(Paths.get(Constants.BUNDLES_DIR))) {

                stream.filter(f -> f.toString().contains(".properties"))
                        .filter(f -> f.toString().contains("messages_"))
                        .forEach(this::readResourceBundle);

            }

            Properties properties = new Properties();

            InputStream iss = new FileInputStream(Constants.DEFAULT_BUNDLE_FILE);
            properties.load(iss);
            localizedMessages.put(Constants.DEFAULT_BUNDLE_KEY, properties);
            log.info("Loaded " + Constants.DEFAULT_BUNDLE_FILE + " resource bundle");

        } catch (IOException e) {
            log.error("IOException occurred:", e);
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
                InputStream iss = new FileInputStream(f.toFile());
                properties.load(iss);
                localizedMessages.put(languageCountry, properties);
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