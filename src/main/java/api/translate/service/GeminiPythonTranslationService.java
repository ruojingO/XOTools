package api.translate.service;

import api.translate.PythonExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeminiPythonTranslationService implements TranslationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiPythonTranslationService.class);
    private static final String TRANSLATION_PROMPT_PREFIX = "pls translate to Chinese:";

    public GeminiPythonTranslationService() {
        LOGGER.info("GeminiPythonTranslationService initialized.");
    }

    @Override
    public String translate(String textToTranslate) throws Exception {
        if (textToTranslate == null || textToTranslate.isEmpty()) {
            LOGGER.warn("Text to translate is null or empty. Returning original text.");
            return textToTranslate;
        }

        // The original PythonExecutor.geminiTrans expects the prompt to be part of the input.
        // It also handles escaping double quotes.
        String promptWithText = TRANSLATION_PROMPT_PREFIX + PythonExecutor.escapeDoubleQuotes(textToTranslate);
        LOGGER.debug("Sending to PythonExecutor.geminiTrans with prompt: '{}'", TRANSLATION_PROMPT_PREFIX);
        
        try {
            // PythonExecutor.geminiTrans makes the call to the Python script.
            // It also has retry logic and error handling (returns original text on failure).
            String translatedText = PythonExecutor.geminiTrans(promptWithText);
            
            // Check if the translation failed (returned original text or contains traceback)
            // The PythonExecutor.geminiTrans currently returns the original prompt if translation fails.
            // We need to be careful not to misinterpret this.
            // A more robust way would be for geminiTrans to throw an exception on failure.
            // For now, we assume if it contains "Traceback" it's an error, or if it's identical to the *original* unescaped text.
            // However, the prompt includes "pls translate to Chinese:", so direct comparison to textToTranslate is not right.
            // The current PythonExecutor.geminiTrans returns the *prompt* (which includes the text) on error.
            if (translatedText.contains("Traceback (most recent call last):") || translatedText.equals(promptWithText)) {
                 LOGGER.warn("Translation via Python script might have failed or returned original prompt for text: '{}'. Translated: '{}'", textToTranslate, translatedText);
                 // Depending on how PythonExecutor.geminiTrans signals failure, this might need adjustment.
                 // If it returns the original *prompt* on failure, we should probably throw an exception or return original *textToTranslate*.
                 // For now, let's assume if it's not a traceback, it's the translation.
                 if (translatedText.contains("Traceback (most recent call last):")) {
                    throw new Exception("Translation failed with Python script error. Original text: " + textToTranslate);
                 }
                 // If it returned the prompt, it means an error occurred in the python script before translation.
                 if (translatedText.equals(promptWithText) && !textToTranslate.startsWith(TRANSLATION_PROMPT_PREFIX)) {
                     LOGGER.warn("Python script returned the input prompt, indicating a possible failure. Original text: {}", textToTranslate);
                     // Decide if this should be an exception or return original text
                     // For consistency with how PythonExecutor handles errors (returns prompt/original text)
                     // we might just return the textToTranslate here after logging.
                     // However, the interface expects an Exception for failure.
                     throw new Exception("Translation failed; Python script returned the input prompt. Original text: " + textToTranslate);
                 }
            }
            
            LOGGER.debug("Successfully translated text via Python script. Original: '{}', Translated: '{}'", textToTranslate, translatedText);
            return translatedText;
        } catch (Exception e) {
            LOGGER.error("Error calling PythonExecutor.geminiTrans for text: '{}'", textToTranslate, e);
            throw e; // Rethrow the exception
        }
    }

    // Optional: Main method for quick testing
    public static void main(String[] args) {
        // This test assumes the Python environment and script are configured as in the original PythonExecutor.
        // Specifically, GOOGLE_GENAI_API_KEY environment variable, Python path, and script path.
        if (System.getenv("GOOGLE_GENAI_API_KEY") == null) {
             System.err.println("Please set the GOOGLE_GENAI_API_KEY environment variable for Gemini testing.");
             //return; // Allow to proceed if API key is not needed for some local test of PythonExecutor
        }
        System.out.println("Note: GeminiPythonTranslationService.main() test depends on Python environment and GOOGLE_GENAI_API_KEY.");

        GeminiPythonTranslationService translator = new GeminiPythonTranslationService();
        try {
            String text1 = "Hello, world!";
            System.out.println("Original: " + text1);
            System.out.println("Translated: " + translator.translate(text1));

            String text2 = "This is a Javadoc comment. /** This is a sample code. */";
            System.out.println("Original: " + text2);
            System.out.println("Translated: " + translator.translate(text2));
            
            // Test with a string that might cause issues if not escaped (though PythonExecutor handles it)
            String text3 = "This is a \"quoted\" string.";
            System.out.println("Original: " + text3);
            System.out.println("Translated: " + translator.translate(text3));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
