# XOTools
## just AI for 8 magga

## Project Overview

XOTools is a Java-based utility project designed for text processing and AI-powered translation tasks. Its main functionalities include:
*   Converting EPUB files to plain text.
*   Translating Javadoc comments within Java source files from their original language (typically English) to Chinese.

## Features

1.  **EPUB to Text Converter:**
    *   Extracts text content from EPUB files.
    *   Uses the Jsoup library for parsing HTML/XHTML content within EPUBs.
    *   Outputs a single plain text file with formatted line wrapping.
    *   (See `src/main/java/EpubToTextWithJsoup.java`)

2.  **Javadoc Translator:**
    *   Traverses specified Java source code directories.
    *   Identifies Javadoc comments and translates their content.
    *   Supports multiple translation backends:
        *   **Gemini:** Utilizes Google's Gemini Pro model via an external Python script (`src/main/java/api/translate/transE.py`).
        *   **GPT-4o:** Employs OpenAI's GPT-4o model via the `chatgpt-java` SDK (a community-maintained Java library).
    *   Allows selective processing of files based on various criteria (all files, modification date, or error content).
    *   (Main logic in `src/main/java/api/translate/AITranslate.java` and `src/main/java/api/translate/JavadocTranslator.java`)

## Setup & Configuration

XOTools is a Maven project. Ensure Maven is installed to build and manage dependencies.

### For Javadoc Translation:

*   **General:**
    *   The system requires Java 8 or higher.
    *   An SLF4J logging implementation (like `slf4j-simple`, included in `pom.xml`) is needed to see log outputs.

*   **Gemini Translator (`gemini` option):**
    *   Relies on a Python environment (Python 3.x).
    *   The Python script `src/main/java/api/translate/transE.py` uses the `google.generativeai` library.
    *   **API Key:** The Google GenAI API key is currently **hardcoded** in `src/main/java/api/translate/PythonExecutor.java`. This is a security risk and should be externalized in a production environment.
    *   The Python script path itself and a Python site-packages path are also currently **hardcoded** within `PythonExecutor.java` and `transE.py` respectively.
    *   Proxy settings for Python HTTP/HTTPS traffic are also hardcoded in `transE.py`.

*   **GPT-4o Translator (`gpt4o` option):**
    *   Requires a valid OpenAI API key.
    *   This key must be set as an environment variable named `OPENAI_API_KEY`.
        *   Example (Linux/macOS): `export OPENAI_API_KEY="your_openai_api_key"`
        *   Example (Windows CMD): `set OPENAI_API_KEY=your_openai_api_key`
        *   Example (Windows PowerShell): `$Env:OPENAI_API_KEY = "your_openai_api_key"`

## How to Build

Build the project using Maven:

```bash
mvn clean package
```
This will compile the code and package it into a JAR file in the `target/` directory (e.g., `XOTools-1.0-SNAPSHOT.jar`). To include all dependencies in a single executable JAR (a "shaded" or "fat" JAR), you might need to configure the `maven-shade-plugin` or `spring-boot-maven-plugin` (if applicable) in the `pom.xml`.

## Usage (`AITranslate` for Javadoc Translation)

The primary entry point for Javadoc translation is the `api.translate.AITranslate` class. It is executed from the command line.

**Command Syntax:**

```bash
java -cp <classpath> api.translate.AITranslate <translator_type> <root_directory> <processing_mode> [exclude_patterns]
```

**Arguments:**

*   `<classpath>`:
    *   The path to the compiled project JAR (e.g., `target/XOTools-1.0-SNAPSHOT.jar`).
    *   If not using a shaded JAR, this must include the project's classes directory (e.g., `target/classes`) and all dependency JARs. You can generate this classpath using `mvn dependency:build-classpath`.
*   `<translator_type>`: Specifies the translation service to use.
    *   `gemini`: Use the Gemini Pro model via the Python script.
    *   `gpt4o`: Use the OpenAI GPT-4o model via the Java SDK.
*   `<root_directory>`: The absolute path to the root directory of the Java source files you want to process.
*   `<processing_mode>`: Defines which files to process.
    *   `all`: Process all `.java` files found in the `<root_directory>`.
    *   `error_fix`: Process files that contain the string "Traceback (most recent call last):" in their content. (Note: The original corrective action for this mode, which involved copying from an "-eng" directory, is currently only a condition check for translation).
    *   `year2019`: Process files last modified in the year 2019.
    *   `by_date`: Process files last modified on January 12, 2024 (hardcoded date from original `transByPatch` logic).
*   `[exclude_patterns]` (Optional):
    *   A comma-separated list of regular expression patterns. Directories or files whose paths match any of these patterns will be excluded from processing.
    *   Example: `com/example/legacy,org/internal/.*`
    *   Default exclusion patterns (if none provided): `"com/sun/.*", "org/omg/.*","java/applet.*","java/awt.*","javax/swing.*","javax/sound.*","javax/print.*"`

**Example Command:**

To translate Javadoc comments in Java files located in `/path/to/my/java_project/src` using GPT-4o, processing all files, and excluding files in `com/example/ignore` directories:

```bash
# Ensure OPENAI_API_KEY is set as an environment variable
java -cp target/XOTools-1.0-SNAPSHOT.jar api.translate.AITranslate gpt4o /path/to/my/java_project/src all "com/example/ignore/.*"
```

## Important Notes

*   **Hardcoded Paths:**
    *   `src/main/java/EpubToTextWithJsoup.java`: Contains hardcoded input/output file paths and a temporary directory path for EPUB processing.
    *   `src/main/java/api/translate/PythonExecutor.java`: Contains a hardcoded path to the `transE.py` script.
    *   `src/main/java/api/translate/transE.py`: Contains a hardcoded `sys.path.append` for Python site-packages and hardcoded proxy settings.
*   **Hardcoded API Key (Gemini):** The `GOOGLE_GENAI_API_KEY` for the Gemini translator is currently **hardcoded** in `src/main/java/api/translate/PythonExecutor.java`. This is a significant security concern and makes the tool less portable. It is strongly recommended to externalize this key (e.g., using environment variables or a configuration file) if using the Gemini translation feature.
*   **Tailored Logic:** The project includes specific processing modes (like date-based conditions) in `AITranslate.java` that might be tailored to particular past use cases or workflows.
*   **Error Handling in `GeminiPythonTranslationService`:** The error detection from the Python script is basic (checks for "Traceback"). More robust error signaling from the Python script would be beneficial.

This README provides a general guide. You may need to adapt paths and configurations based on your specific setup.
