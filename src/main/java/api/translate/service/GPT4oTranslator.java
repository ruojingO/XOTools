package api.translate.service;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
// import com.unfbx.chatgpt.entity.chat.ChatCompletion.Model; // Model is now referenced by string
import okhttp3.OkHttpClient; // For proxy configuration
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GPT4oTranslator implements TranslationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GPT4oTranslator.class);
    private final OpenAiClient openAiClient;
    private static final String GPT_4_O_MODEL_NAME = "gpt-4o";

    public GPT4oTranslator() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            LOGGER.error("OPENAI_API_KEY environment variable is not set.");
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set.");
        }
        List<String> apiKeys = Arrays.asList(apiKey);

        String proxyHost = System.getenv("OPENAI_PROXY_HOST");
        String proxyPortStr = System.getenv("OPENAI_PROXY_PORT");
        
        OpenAiClient.Builder clientBuilder = OpenAiClient.builder().apiKey(apiKeys);

        if (proxyHost != null && !proxyHost.isEmpty() && proxyPortStr != null && !proxyPortStr.isEmpty()) {
            LOGGER.info("Found proxy settings: OPENAI_PROXY_HOST='{}', OPENAI_PROXY_PORT='{}'", proxyHost, proxyPortStr);
            try {
                int proxyPort = Integer.parseInt(proxyPortStr);
                if (proxyPort > 0 && proxyPort <= 65535) {
                    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                    okHttpClientBuilder.proxy(proxy);
                    // It's good practice to set timeouts for the client
                    okHttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
                    okHttpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);
                    okHttpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
                    
                    OkHttpClient customOkHttpClient = okHttpClientBuilder.build();
                    clientBuilder.okHttpClient(customOkHttpClient);
                    LOGGER.info("Proxy configured for host {} and port {}", proxyHost, proxyPort);
                } else {
                    LOGGER.warn("Invalid proxy port number: {}. Must be between 1 and 65535. Proceeding without proxy.", proxyPortStr);
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid proxy port format: '{}'. Must be an integer. Proceeding without proxy.", proxyPortStr, e);
            }
        } else {
            LOGGER.info("No proxy settings (OPENAI_PROXY_HOST, OPENAI_PROXY_PORT) found or one is missing. Proceeding without custom proxy.");
        }
        
        this.openAiClient = clientBuilder.build();
        LOGGER.info("GPT4oTranslator initialized successfully (proxy status logged above).");
    }

    @Override
    public String translate(String textToTranslate) throws Exception {
        if (textToTranslate == null || textToTranslate.isEmpty()) {
            LOGGER.warn("Text to translate is null or empty. Returning original text.");
            return textToTranslate;
        }

        LOGGER.debug("Attempting to translate text: '{}'", textToTranslate);

        Message systemMessage = Message.builder()
                .role(Message.Role.SYSTEM)
                .content("You are a helpful translation assistant. Translate the user's text from its original language to Chinese. Provide only the translated text.")
                .build();

        Message userMessage = Message.builder()
                .role(Message.Role.USER)
                .content(textToTranslate)
                .build();

        ChatCompletion chatCompletionRequest = ChatCompletion.builder()
                .model(GPT_4_O_MODEL_NAME) // Use the string "gpt-4o"
                .messages(Arrays.asList(systemMessage, userMessage))
                .temperature(0.7) // Adjust as needed
                .maxTokens(2000)  // Adjust based on expected length, Javadoc comments can be long
                .build();

        try {
            ChatCompletionResponse response = openAiClient.chatCompletion(chatCompletionRequest);
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String translatedText = response.getChoices().get(0).getMessage().getContent();
                LOGGER.debug("Successfully translated text. Original: '{}', Translated: '{}'", textToTranslate, translatedText);
                return translatedText.trim();
            } else {
                LOGGER.error("Received no valid translation from API. Response: {}", response);
                throw new Exception("No translation received or error in API response. Raw response: " + Objects.toString(response));
            }
        } catch (Exception e) {
            LOGGER.error("Error during API call to GPT-4o for text: '{}'", textToTranslate, e);
            // Rethrow the exception to be handled by the caller, or return original text as fallback
            throw e; // Or return textToTranslate; depending on desired error handling
        }
    }

    // Optional: Main method for quick testing
    public static void main(String[] args) {
        // Ensure OPENAI_API_KEY is set in your environment variables before running
        if (System.getenv("OPENAI_API_KEY") == null) {
            System.err.println("Please set the OPENAI_API_KEY environment variable.");
            return;
        }

        GPT4oTranslator translator = new GPT4oTranslator();
        try {
            String text1 = "Hello, world!";
            System.out.println("Original: " + text1);
            System.out.println("Translated: " + translator.translate(text1));

            String text2 = "This is a Javadoc comment. /** This is a sample code. */";
            System.out.println("Original: " + text2);
            System.out.println("Translated: " + translator.translate(text2));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
