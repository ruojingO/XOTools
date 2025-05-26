# GPT4oTranslator.java 单元测试大纲

本文档概述了 `GPT4oTranslator.java` 的测试用例。这些测试将酌情使用 Mockito 来模拟 `OpenAiClient` 和其他依赖项。

## 1. 初始化测试

*   **测试用例 1.1: 初始化成功**
    *   **条件:** `OPENAI_API_KEY` 环境变量已设置有效的（虚拟）密钥。
    *   **操作:** 实例化 `GPT4oTranslator`。
    *   **预期结果:** `GPT4oTranslator` 对象成功创建，无异常抛出。内部的 `OpenAiClient` 应已初始化。
    *   **模拟:** 如果 `OpenAiClient.builder()` 链执行了此特定单元测试中不希望的即时验证，则可能需要模拟它，不过使用有效密钥直接实例化应该是可测试的。

*   **测试用例 1.2: 未设置API密钥**
    *   **条件:** `OPENAI_API_KEY` 环境变量未设置或为空。
    *   **操作:** 尝试实例化 `GPT4oTranslator`。
    *   **预期结果:** 抛出 `IllegalStateException`，消息指示API密钥未设置。

## 2. 翻译测试

*   **测试用例 2.1: 翻译成功**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本有效。
    *   **操作:** 调用 `translate("Hello, world!")`。
    *   **模拟:**
        *   模拟由构建器返回的 `OpenAiClient`。
        *   模拟 `openAiClient.chatCompletion(ChatCompletion)` 返回一个 `ChatCompletionResponse`，其中包含一个模拟的 `Choice`，该`Choice`又包含一个带有预期翻译文本（例如，“你好，世界！”）的 `Message`。
    *   **预期结果:** 方法返回翻译后的字符串“你好，世界！”。

*   **测试用例 2.2: 翻译空字符串**
    *   **条件:** `OPENAI_API_KEY` 已设置。
    *   **操作:** 调用 `translate("")`。
    *   **模拟:** 不应调用 `OpenAiClient`。
    *   **预期结果:** 方法返回空字符串（根据当前实现）。不应进行API调用。

*   **测试用例 2.3: 翻译Null输入**
    *   **条件:** `OPENAI_API_KEY` 已设置。
    *   **操作:** 调用 `translate(null)`。
    *   **模拟:** 不应调用 `OpenAiClient`。
    *   **预期结果:** 方法返回 `null`（根据当前实现）。不应进行API调用。

## 3. API错误处理测试

*   **测试用例 3.1: API调用抛出异常**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本有效。
    *   **操作:** 调用 `translate("Test text")`。
    *   **模拟:**
        *   模拟 `OpenAiClient`。
        *   配置 `openAiClient.chatCompletion(ChatCompletion)` 抛出特定的运行时异常 (例如, `RuntimeException("API Error")`)。
    *   **预期结果:** `translate` 方法重新抛出从 `OpenAiClient` 收到的异常。

*   **测试用例 3.2: API返回Null响应**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本有效。
    *   **操作:** 调用 `translate("Test text")`。
    *   **模拟:**
        *   模拟 `OpenAiClient`。
        *   配置 `openAiClient.chatCompletion(ChatCompletion)` 返回 `null`。
    *   **预期结果:** `translate` 方法抛出 `Exception` (根据当前实现: "No translation received or error in API response")。

*   **测试用例 3.3: API返回带Null Choices的响应**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本有效。
    *   **操作:** 调用 `translate("Test text")`。
    *   **模拟:**
        *   模拟 `OpenAiClient`。
        *   模拟 `ChatCompletionResponse`，其中 `getChoices()` 返回 `null`。
        *   配置 `openAiClient.chatCompletion(ChatCompletion)` 返回此模拟响应。
    *   **预期结果:** `translate` 方法抛出 `Exception`。

*   **测试用例 3.4: API返回带空Choices列表的响应**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本有效。
    *   **操作:** 调用 `translate("Test text")`。
    *   **模拟:**
        *   模拟 `OpenAiClient`。
        *   模拟 `ChatCompletionResponse`，其中 `getChoices()` 返回空列表。
        *   配置 `openAiClient.chatCompletion(ChatCompletion)` 返回此模拟响应。
    *   **预期结果:** `translate` 方法抛出 `Exception`。

*   **测试用例 3.5: API返回带Null Message的Choice**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本有效。
    *   **操作:** 调用 `translate("Test text")`。
    *   **模拟:**
        *   模拟 `OpenAiClient`。
        *   模拟 `ChatCompletionResponse`, `Choice`。
        *   配置 `Choice.getMessage()` 返回 `null`。
        *   配置 `openAiClient.chatCompletion(ChatCompletion)` 返回包含此choice的响应。
    *   **预期结果:** `translate` 方法根据其访问方式抛出 `Exception` 或 `NullPointerException` (当前代码可能会抛出NPE，应通过抛出自定义Exception来优雅处理)。

*   **测试用例 3.6: API返回Message内容为Null的Choice**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本有效。
    *   **操作:** 调用 `translate("Test text")`。
    *   **模拟:**
        *   模拟 `OpenAiClient`。
        *   模拟 `ChatCompletionResponse`, `Choice`, `Message`。
        *   配置 `Message.getContent()` 返回 `null`。
        *   配置 `openAiClient.chatCompletion(ChatCompletion)` 返回包含此choice和message的响应。
    *   **预期结果:** 如果此情况被视为错误，方法可能返回 `null` 或空字符串，或抛出异常。应明确定义并测试预期行为。当前实现将返回 `null.trim()`，导致NPE。此问题应得到处理。

## 4. 请求参数验证

*   **测试用例 4.1: 正确的模型和消息传递给API**
    *   **条件:** `OPENAI_API_KEY` 已设置。输入文本为 "Translate this."
    *   **操作:** 调用 `translate("Translate this.")`。
    *   **模拟:**
        *   模拟 `OpenAiClient`。
        *   对 `openAiClient.chatCompletion()` 方法使用 `ArgumentCaptor<ChatCompletion>`。
    *   **预期结果:**
        *   验证捕获的 `ChatCompletion` 请求使用模型 "gpt-4o"。
        *   验证消息列表包含两条消息：
            *   一条角色为 `SYSTEM` 的系统消息，内容为 "You are a helpful translation assistant. Translate the user's text from its original language to Chinese. Provide only the translated text."
            *   一条角色为 `USER` 的用户消息，内容为 "Translate this."
        *   如果对本测试至关重要，验证其他参数，如temperature和maxTokens。

## 注意事项：
*   考虑使用JUnit 5进行这些测试。
*   可以使用像 `SystemLambda` 这样的库来模拟 `System.getenv()`，或者如果直接模拟环境比较困难，可以将API密钥检索重构为一个单独的、可模拟的组件。对于单元测试，在测试执行环境中临时设置环境变量，或者将密钥作为构造函数参数传递给辅助类可能更简单。然而，当前的 `GPT4oTranslator` 直接调用 `System.getenv`。
*   确保 `Logger` 交互不会导致问题 (例如，为测试提供简单的SLF4J绑定，或者如果其交互复杂则模拟logger)。
