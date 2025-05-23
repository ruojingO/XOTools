package api.translate;

import api.translate.service.GPT4oTranslator;
import api.translate.service.GeminiPythonTranslationService;
import api.translate.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AITranslate {
    private static final Logger LOGGER = LoggerFactory.getLogger(AITranslate.class);
    private final List<Pattern> excludedDirectoriesPatterns = new ArrayList<>();
    private int javaFileCounter = 0;
    private final JavadocTranslator javadocTranslator;
    private final String processingMode; // e.g., "all", "by_date", "error_fix"

    public AITranslate(TranslationService translationService, List<String> excludePatterns, String processingMode) {
        this.javadocTranslator = new JavadocTranslator(translationService);
        initializeExcludedDirectories(excludePatterns);
        this.processingMode = processingMode != null ? processingMode : "all"; // Default to "all"
        LOGGER.info("AITranslate initialized with service: {}, mode: {}",
                translationService.getClass().getSimpleName(), this.processingMode);
    }

    private void initializeExcludedDirectories(List<String> patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                try {
                    excludedDirectoriesPatterns.add(Pattern.compile(pattern));
                } catch (Exception e) {
                    LOGGER.error("Invalid regex pattern for exclusion: {}", pattern, e);
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.error("Usage: AITranslate <gemini|gpt4o> <rootDir> [processingMode] [excludePattern1,excludePattern2,...]");
            System.err.println("Usage: AITranslate <gemini|gpt4o> <rootDir> [processingMode] [excludePattern1,excludePattern2,...]");
            System.err.println("Example: AITranslate gpt4o /path/to/java/src all com/example/legacy,org/ignore");
            System.err.println("Processing modes: all, by_date (year 2024, month 1, day 12), error_fix (contains 'Traceback')");
            return;
        }

        String translatorType = args[0].toLowerCase();
        String rootDirPath = args[1];
        String mode = (args.length > 2) ? args[2] : "all"; // Default processing mode
        List<String> excludePatterns = new ArrayList<>();
        if (args.length > 3 && args[3] != null && !args[3].isEmpty()) {
            excludePatterns = Arrays.asList(args[3].split(","));
        }
         // Default exclude patterns from original code if none provided via CLI
        if (excludePatterns.isEmpty()) {
            excludePatterns = Arrays.asList("com/sun/.*", "org/omg/.*","java/applet.*","java/awt.*","javax/swing.*","javax/sound.*","javax/print.*");
        }


        TranslationService service;
        if ("gemini".equals(translatorType)) {
            service = new GeminiPythonTranslationService();
        } else if ("gpt4o".equals(translatorType)) {
            // Ensure OPENAI_API_KEY is set for GPT4oTranslator
            if (System.getenv("OPENAI_API_KEY") == null || System.getenv("OPENAI_API_KEY").isEmpty()) {
                LOGGER.error("OPENAI_API_KEY environment variable must be set to use the gpt4o translator.");
                System.err.println("Error: OPENAI_API_KEY environment variable must be set to use the gpt4o translator.");
                return;
            }
            service = new GPT4oTranslator();
        } else {
            LOGGER.error("Invalid translator type specified: {}. Choose 'gemini' or 'gpt4o'.", translatorType);
            System.err.println("Invalid translator type specified. Choose 'gemini' or 'gpt4o'.");
            return;
        }

        File rootDir = new File(rootDirPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            LOGGER.error("Root directory does not exist or is not a directory: {}", rootDirPath);
            System.err.println("Root directory does not exist or is not a directory: " + rootDirPath);
            return;
        }

        AITranslate aiTranslate = new AITranslate(service, excludePatterns, mode);
        try {
            aiTranslate.traverseDirectory(rootDir);
            LOGGER.info("Finished processing all files. Total Java files processed: {}", aiTranslate.javaFileCounter);
        } catch (IOException e) {
            LOGGER.error("An error occurred during directory traversal or file processing.", e);
        }
    }


    private void traverseDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!isExcludedDirectory(file)) {
                        traverseDirectory(file);
                    } else {
                        LOGGER.debug("Skipping excluded directory: {}", file.getAbsolutePath());
                    }
                } else if (file.getName().endsWith(".java")) {
                    // Apply processing logic based on the mode
                    boolean shouldProcess = false;
                    switch (processingMode.toLowerCase()) {
                        case "all":
                            shouldProcess = true;
                            break;
                        case "by_date":
                            if (checkDateCondition(file)) { // Corresponds to old transByPatch
                                shouldProcess = true;
                            }
                            break;
                        case "error_fix":
                             if (checkErrorContentCondition(file)) { // Corresponds to old dealTransError
                                shouldProcess = true;
                            }
                            break;
                        case "year2019": // Corresponds to old deal2019
                            if (checkYear2019Condition(file)) {
                                shouldProcess = true;
                            }
                            break;
                        default:
                            LOGGER.warn("Unknown processing mode: '{}'. Defaulting to processing all Java files.", processingMode);
                            shouldProcess = true; // Default to processing if mode is unrecognized
                            break;
                    }
                    if (shouldProcess) {
                        processFile(file);
                    } else {
                        LOGGER.debug("Skipping file due to processing mode constraints: {}", file.getAbsolutePath());
                    }
                }
            }
        }
    }
    
    private void processFile(File file) {
        javaFileCounter++;
        LOGGER.info("Processing Java File #{}: {}", javaFileCounter, file.getAbsolutePath());
        try {
            javadocTranslator.coreTrans(file);
        } catch (IOException e) {
            LOGGER.error("Error processing file {}: {}", file.getAbsolutePath(), e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during translation of file {}: {}", file.getAbsolutePath(), e.getMessage(), e);
        }
    }


    private boolean checkDateCondition(File file) throws IOException { // Was transByPatch
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        LocalDate modifiedDate = attrs.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        boolean conditionMet = modifiedDate.getYear() == 2024 && modifiedDate.getMonthValue() == 1 && modifiedDate.getDayOfMonth() == 12;
        if (conditionMet) {
            LOGGER.info("File matches date condition (2024-01-12): {}", file.getName());
        }
        return conditionMet;
    }

    private boolean checkErrorContentCondition(File file) throws IOException { // Was dealTransError
        // This method originally also performed a replacement if "Traceback" was found.
        // For now, it only checks the condition. The actual fix (if any) would be part of JavadocTranslator
        // or a subsequent step if translation itself is the fix.
        Path filePath = file.toPath();
        String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        boolean conditionMet = fileContent.contains("Traceback (most recent call last):");
        if (conditionMet) {
            LOGGER.info("File contains 'Traceback (most recent call last):': {}", file.getName());
            // Original logic also did a replacement:
            // String xfp = filePath.toString().replace("\\src\\", "\\src-eng\\");
            // String nfileContent = new String(Files.readAllBytes(Paths.get(xfp)), StandardCharsets.UTF_8); // Assuming xfp exists
            // Files.write(Paths.get(filePath.toString()), nfileContent.getBytes(StandardCharsets.UTF_8));
            // This part is complex as it implies copying from a corresponding "-eng" directory.
            // For this refactoring, we focus on the translation selection. This logic might need to be handled separately.
            // For now, just returning the condition.
        }
        return conditionMet;
    }
    
    private boolean checkYear2019Condition(File file) throws IOException { // Was deal2019
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        LocalDate modifiedDate = attrs.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        boolean conditionMet = modifiedDate.getYear() == 2019;
         if (conditionMet) {
            LOGGER.info("File matches date condition (Year 2019): {}", file.getName());
        }
        return conditionMet;
    }


    private boolean isExcludedDirectory(File dir) {
        String path = dir.getPath().replace('\\', '/');
        for (Pattern pattern : excludedDirectoriesPatterns) {
            if (pattern.matcher(path).find()) {
                LOGGER.debug("Directory {} matches exclude pattern {}", path, pattern.pattern());
                return true;
            }
        }
        return false;
    }
}

