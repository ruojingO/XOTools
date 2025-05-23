package api.translate;

import api.translate.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavadocTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavadocTranslator.class);
    private final TranslationService translationService;
    private static final int MIN_JAVADOC_LENGTH_FOR_TRANSLATION = 20; // Configurable minimum length
    private static final long API_CALL_DELAY_MS = 200; // Delay between API calls

    public JavadocTranslator(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void coreTrans(File file) throws IOException {
        Path filePath = file.toPath();
        LOGGER.info("Processing file for Javadoc translation: {}", filePath);
        String fileContent;
        try {
            fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to read file: {}", filePath, e);
            throw e;
        }

        Pattern javadocPattern = Pattern.compile("/\\*\\*.*?\\*/", Pattern.DOTALL);
        Matcher javadocMatcher = javadocPattern.matcher(fileContent);
        StringBuilder newFileContent = new StringBuilder();
        int lastEnd = 0;

        while (javadocMatcher.find()) {
            newFileContent.append(fileContent, lastEnd, javadocMatcher.start());
            String javadocComment = javadocMatcher.group();
            String originalJavadocContent = extractJavadocContent(javadocComment);
            String translatedJavadocContent = originalJavadocContent; 

            if (originalJavadocContent.length() > MIN_JAVADOC_LENGTH_FOR_TRANSLATION) {
                LOGGER.debug("Javadoc segment to translate (length {}): {}", originalJavadocContent.length(), originalJavadocContent);
                try {
                    translatedJavadocContent = translationService.translate(originalJavadocContent);
                    LOGGER.debug("Translated Javadoc content: {}", translatedJavadocContent);
                    Thread.sleep(API_CALL_DELAY_MS);
                } catch (Exception e) {
                    LOGGER.error("Failed to translate Javadoc segment for file {}: '{}'. Error: {}",
                            filePath, originalJavadocContent, e.getMessage(), e);
                    translatedJavadocContent = originalJavadocContent; // Keep original on error
                }
            } else {
                LOGGER.debug("Javadoc segment is too short (length {}), skipping translation: {}",
                             originalJavadocContent.length(), originalJavadocContent);
            }
            
            newFileContent.append("/**\n * ").append(translatedJavadocContent.replace("\n", "\n * ")).append("\n */");
            lastEnd = javadocMatcher.end();
        }
        newFileContent.append(fileContent.substring(lastEnd));

        try {
            Files.write(Paths.get(filePath.toString()), newFileContent.toString().getBytes(StandardCharsets.UTF_8));
            LOGGER.info("Successfully processed and rewrote file: {}", filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to write translated content to file: {}", filePath, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during file writing for {}: {}", filePath, e.getMessage(), e);
            throw new IOException("Unexpected error writing file " + filePath, e);
        }
    }

    private String extractJavadocContent(String javadocComment) {
        String content = javadocComment.replaceAll("^/\\*\\*|\\*/$", ""); 
        content = content.replaceAll("\n\\s*\\* ?", "\n"); 
        return content.trim();
    }
}
