# XOTools
## 八正道AI工具

## 项目概述

XOTools 是一个基于Java的实用工具项目，专为文本处理和AI驱动的翻译任务而设计。其主要功能包括：
*   将EPUB文件转换为纯文本。
*   将Java源文件中的Javadoc注释从其原始语言（通常是英语）翻译成中文。

## 功能特性

1.  **EPUB到文本转换器：**
    *   从EPUB文件中提取文本内容。
    *   使用Jsoup库解析EPUB中的HTML/XHTML内容。
    *   输出带有格式化换行的单个纯文本文件。
    *   (参见 `src/main/java/EpubToTextWithJsoup.java`)

2.  **Javadoc翻译器：**
    *   遍历指定的Java源代码目录。
    *   识别Javadoc注释并翻译其内容。
    *   支持多种翻译后端：
        *   **Gemini:** 通过外部Python脚本 (`src/main/java/api/translate/transE.py`) 利用谷歌的Gemini Pro模型。
        *   **GPT-4o:** 通过 `chatgpt-java` SDK（一个社区维护的Java库）使用OpenAI的GPT-4o模型。
    *   允许基于多种标准（所有文件、修改日期或错误内容）选择性处理文件。
    *   (主要逻辑在 `src/main/java/api/translate/AITranslate.java` 和 `src/main/java/api/translate/JavadocTranslator.java`)

## 设置与配置

XOTools是一个Maven项目。请确保已安装Maven以构建和管理依赖项。

### Javadoc翻译功能配置：

*   **通用：**
    *   系统要求Java 8或更高版本。
    *   需要SLF4J日志实现（如`pom.xml`中包含的`slf4j-simple`）才能查看日志输出。

*   **Gemini翻译器 (`gemini` 选项)：**
    *   依赖Python环境（Python 3.x）。
    *   Python脚本 `src/main/java/api/translate/transE.py` 使用 `google.generativeai` 库。
    *   **API密钥：** Google GenAI API密钥目前**硬编码**在 `src/main/java/api/translate/PythonExecutor.java` 中。这是一个安全风险，在生产环境中应将其外部化。
    *   Python脚本路径本身和Python site-packages路径目前也分别**硬编码**在 `PythonExecutor.java` 和 `transE.py` 中。
    *   `transE.py`中还硬编码了Python HTTP/HTTPS流量的代理设置。

*   **GPT-4o翻译器 (`gpt4o` 选项)：**
        *   需要一个有效的OpenAI API密钥。
        *   此密钥必须设置为名为 `OPENAI_API_KEY` 的环境变量。
            *   示例 (Linux/macOS): `export OPENAI_API_KEY="your_openai_api_key"`
            *   示例 (Windows CMD): `set OPENAI_API_KEY=your_openai_api_key`
            *   示例 (Windows PowerShell): `$Env:OPENAI_API_KEY = "your_openai_api_key"`
        *   **可选代理配置 (如果需要)：**
            如果您的环境需要HTTP代理才能访问OpenAI API，您可以使用以下环境变量进行配置：
            *   `OPENAI_PROXY_HOST`: 您的代理服务器主机名或IP地址 (例如, `127.0.0.1`, `proxy.example.com`)。
            *   `OPENAI_PROXY_PORT`: 您的代理服务器端口号 (例如, `1080`, `8080`)。
            应用程序将自动使用这些设置通过指定的代理路由API请求。如果未设置这些变量，或者端口无效，应用程序将尝试直接连接。

## 如何构建

使用Maven构建项目：

```bash
mvn clean package
```
这将编译代码并将项目打包到 `target/` 目录下的JAR文件中 (例如, `XOTools-1.0-SNAPSHOT.jar`)。如果需要将所有依赖项包含在单个可执行JAR（“shaded” 或 “fat” JAR）中，您可能需要在 `pom.xml` 中配置 `maven-shade-plugin` 或 `spring-boot-maven-plugin` (如果适用)。

## 使用方法 (`AITranslate` 进行Javadoc翻译)

Javadoc翻译的主要入口点是 `api.translate.AITranslate` 类。它通过命令行执行。

**命令语法：**

```bash
java -cp <classpath> api.translate.AITranslate <translator_type> <root_directory> <processing_mode> [exclude_patterns]
```

**参数说明：**

*   `<classpath>`:
    *   指向已编译项目JAR的路径 (例如, `target/XOTools-1.0-SNAPSHOT.jar`)。
    *   如果不使用shaded JAR，则此路径必须包含项目的类目录 (例如, `target/classes`) 和所有依赖JAR。您可以使用 `mvn dependency:build-classpath` 生成此 classpath。
*   `<translator_type>`: 指定要使用的翻译服务。
    *   `gemini`: 通过Python脚本使用Gemini Pro模型。
    *   `gpt4o`: 通过Java SDK使用OpenAI GPT-4o模型。
*   `<root_directory>`: 您要处理的Java源文件根目录的绝对路径。
*   `<processing_mode>`: 定义要处理的文件。
    *   `all`: 处理在 `<root_directory>` 中找到的所有 `.java` 文件。
    *   `error_fix`: 处理内容中包含字符串 "Traceback (most recent call last):" 的文件。(注意：此模式的原始纠正措施涉及从“-eng”目录复制，目前仅作为翻译的条件检查)。
    *   `year2019`: 处理最后修改于2019年的文件。
    *   `by_date`: 处理最后修改于2024年1月12日的文件 (硬编码日期，源自原始 `transByPatch` 逻辑)。
*   `[exclude_patterns]` (可选):
    *   逗号分隔的正则表达式列表。路径与这些模式中任何一个匹配的目录或文件将从处理中排除。
    *   示例: `com/example/legacy,org/internal/.*`
    *   默认排除模式 (如果未提供): `"com/sun/.*", "org/omg/.*","java/applet.*","java/awt.*","javax/swing.*","javax/sound.*","javax/print.*"`

**示例命令：**

要使用GPT-4o翻译位于 `/path/to/my/java_project/src` 中的Java文件的Javadoc注释，处理所有文件，并排除 `com/example/ignore` 目录中的文件：

```bash
# 确保OPENAI_API_KEY已设置为环境变量
java -cp target/XOTools-1.0-SNAPSHOT.jar api.translate.AITranslate gpt4o /path/to/my/java_project/src all "com/example/ignore/.*"
```

## 重要说明

*   **硬编码路径：**
    *   `src/main/java/EpubToTextWithJsoup.java`: 包含用于EPUB处理的硬编码输入/输出文件路径和临时目录路径。
    *   `src/main/java/api/translate/PythonExecutor.java`: 包含指向 `transE.py` 脚本的硬编码路径。
    *   `src/main/java/api/translate/transE.py`: 包含用于Python site-packages的硬编码 `sys.path.append` 和硬编码的代理设置。
*   **硬编码API密钥 (Gemini)：** Gemini翻译器的 `GOOGLE_GENAI_API_KEY` 目前**硬编码**在 `src/main/java/api/translate/PythonExecutor.java` 中。这是一个重大的安全问题，并降低了工具的可移植性。如果使用Gemini翻译功能，强烈建议将此密钥外部化 (例如，使用环境变量或配置文件)。
*   **定制逻辑：** 项目在 `AITranslate.java` 中包含特定的处理模式 (如基于日期的条件)，这些模式可能是为特定历史用例或工作流程量身定制的。
*   **`GeminiPythonTranslationService` 中的错误处理：** 从Python脚本进行的错误检测是基础的 (检查 "Traceback")。从Python脚本发出更强大的错误信号将是有益的。

本README提供一般性指导。您可能需要根据您的特定设置调整路径和配置。
