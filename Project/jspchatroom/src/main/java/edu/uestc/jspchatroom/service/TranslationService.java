package edu.uestc.jspchatroom.service;

import edu.uestc.jspchatroom.dao.DAOException;
import edu.uestc.jspchatroom.dao.TranslationDao;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public final class TranslationService {
    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());
    private static final Set<String> LANGUAGES = Set.of("zh", "en", "ja", "ko");

    private final TranslationDao translations = new TranslationDao();
    private final MyMemoryProvider provider = new MyMemoryProvider();
    private final Object[] locks = IntStream.range(0, 64).mapToObj(i -> new Object()).toArray();

    public static boolean supports(String language) {
        return language != null && LANGUAGES.contains(language);
    }

    public String translate(int messageId, String target, String sourceLanguage)
            throws IOException, InterruptedException {
        String original = translations.findMessageContent(messageId);
        if (original == null) return null;

        String source = sourceLanguage;
        if (source == null || source.equals("auto")) source = MyMemoryProvider.detectLanguage(original);
        if (!supports(source)) {
            throw new IllegalArgumentException("原文语言无效 / Invalid source language");
        }
        // Reuse existing translations across the refactor.
        String key = "v2:" + source + ":" + target;
        synchronized (locks[(key.hashCode() ^ messageId) & (locks.length - 1)]) {
            String cached = translations.findTranslation(messageId, key);
            if (cached != null) return cached;

            String text = provider.translate(original, target, source);
            try {
                translations.saveTranslation(messageId, key, text);
            } catch (DAOException e) {
                LOGGER.log(Level.WARNING, "Could not cache translation for message " + messageId, e);
            }
            return text;
        }
    }
}
