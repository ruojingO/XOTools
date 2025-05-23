## OpenAI Java SDK Research (Community SDK: Grt1228/chatgpt-java)

OpenAI does not currently offer an official Java SDK in the same vein as its Python or Node.js libraries. However, the community has developed several options. `Grt1228/chatgpt-java` is a popular and comprehensive choice.

**1. GitHub Repository URL & Documentation:**

*   **GitHub Repository:** [https://github.com/Grt1228/chatgpt-java](https://github.com/Grt1228/chatgpt-java)
*   **Official Documentation (Chinese, with some English parts):** [https://chatgpt-java.unfbx.com/](https://chatgpt-java.unfbx.com/)
*   **English README:** [https://github.com/Grt1228/chatgpt-java/blob/develop/README_EN.md](https://github.com/Grt1228/chatgpt-java/blob/develop/README_EN.md)

**2. Maven Dependency Snippet:**

To add this library to your project, include the following dependency in your `pom.xml`. It's recommended to check the [Releases page](https://github.com/Grt1228/chatgpt-java/releases) on GitHub for the latest version. The README mentions `1.0.14-beta1`, but `v1.1.5` was the latest at the time of this research.

```xml
<dependency>
    <groupId>com.unfbx</groupId>
    <artifactId>chatgpt-java</artifactId>
    <version>1.1.5</version> <!-- Replace with the latest version -->
</dependency>
```

**3. Client Initialization (API Key from Environment Variable):**

The SDK allows you to configure the client using a builder pattern. To use an API key from an environment variable:

```java
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.function.KeyRandomStrategy; // Or FirstKeyStrategy for specific key selection
import java.util.Arrays;
import java.util.List;

public class OpenAiClientExample {
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: OPENAI_API_KEY environment variable is not set.");
            return;
        }

        // The SDK expects a list of API keys.
        List<String> apiKeys = Arrays.asList(apiKey);

        OpenAiClient openAiClient = OpenAiClient.builder()
                .apiKey(apiKeys)
                // .keyStrategy(new FirstKeyStrategy()) // Strategy for selecting a key if multiple are provided.
                                                    // KeyRandomStrategy is default.
                // .apiHost("https://your-proxy-api.com/") // Optional: If you use a proxy.
                // .okHttpClient(yourCustomOkHttpClient) // Optional: For custom OkHttpClient settings
                .build();

        System.out.println("OpenAiClient initialized successfully.");
        // You can now use openAiClient to interact with the OpenAI API
    }
}
```
*   Retrieve the API key using `System.getenv("OPENAI_API_KEY")`.
*   The `.apiKey()` method accepts a `List<String>` of API keys.
*   You can specify a `keyStrategy` if providing multiple keys.

**4. Chat Completion Request for Translation:**

To perform a translation, you can use the chat completion endpoint by providing a system message to define the AI's role and a user message containing the text to translate.

```java
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.ChatCompletion.Model; // Enum for models
import java.util.Arrays;
import java.util.List;

public class TranslationExample {
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("OPENAI_API_KEY not set.");
            return;
        }

        OpenAiClient openAiClient = OpenAiClient.builder()
                .apiKey(Arrays.asList(apiKey))
                .build();

        // 1. Define the role of the AI with a system message
        Message systemMessage = Message.builder()
                .role(Message.Role.SYSTEM)
                .content("You are a helpful translation assistant. Translate the user's text from English to Chinese.")
                .build();

        // 2. Provide the text to translate as a user message
        String textToTranslate = "Hello, how are you today?";
        Message userMessage = Message.builder()
                .role(Message.Role.USER)
                .content(textToTranslate)
                .build();

        // 3. Create the chat completion request
        ChatCompletion chatCompletionRequest = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO.getName()) // Or use Model.GPT_4.getName() etc.
                .messages(Arrays.asList(systemMessage, userMessage))
                // .temperature(0.7) // Optional: Adjust for more creative or deterministic responses
                // .maxTokens(150)   // Optional: Limit the length of the translated output
                .build();

        try {
            // 4. Make the API call
            ChatCompletionResponse response = openAiClient.chatCompletion(chatCompletionRequest);

            // 5. Process the response
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String translatedText = response.getChoices().get(0).getMessage().getContent();
                System.out.println("Original: " + textToTranslate);
                System.out.println("Translated: " + translatedText);
            } else {
                System.out.println("No translation received or error in response.");
            }
        } catch (Exception e) {
            System.err.println("Error during API call: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

This SDK provides comprehensive access to OpenAI API features, including streaming, function calling, and more, as detailed in its documentation. Remember to handle potential exceptions and errors, such as API key issues, network problems, or rate limits.
