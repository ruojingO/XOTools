# Guide: Running XOTools Translations

This guide provides instructions on how to use XOTools to translate Javadoc comments within the project's Java source code and to translate the project's Markdown documentation files.

## Prerequisites

Before you begin, please ensure you have the following set up:

1.  **Latest Project Version:**
    *   Make sure your local copy of the XOTools project is cloned and updated to the latest commit. This version should contain the `MarkdownTranslator.java` utility and the updated `AITranslate` class supporting different translation services and command-line arguments.

2.  **Environment Variables:**
    *   **`OPENAI_API_KEY` (Mandatory for GPT-4o):** This environment variable must be set to your valid OpenAI API key.
        *   Linux/macOS: `export OPENAI_API_KEY="your_openai_api_key"`
        *   Windows CMD: `set OPENAI_API_KEY=your_openai_api_key`
        *   Windows PowerShell: `$Env:OPENAI_API_KEY = "your_openai_api_key"`
    *   **Proxy Configuration (Optional):** If you require an HTTP proxy to access the OpenAI API, set the following environment variables:
        *   `OPENAI_PROXY_HOST`: Your proxy server's hostname or IP address (e.g., `127.0.0.1`).
        *   `OPENAI_PROXY_PORT`: Your proxy server's port number (e.g., `8080`).
        If these are not set, the GPT-4o translator will attempt to connect directly.

3.  **Build the Project:**
    *   Compile the project and package it using Maven to ensure the executable JAR is up-to-date. Run the following command from the project's root directory:
        ```bash
        mvn clean package
        ```
    *   This should generate a JAR file in the `target/` directory, typically named `XOTools-1.0-SNAPSHOT.jar`. If your JAR name differs (e.g., includes `-jar-with-dependencies`), please adjust the classpath in the commands below accordingly. For the commands below, we will assume the JAR is `target/XOTools-1.0-SNAPSHOT.jar` and that it's a "thin" JAR, so we will construct a full classpath.

4.  **Full Classpath (Important for running the tools):**
    *   The `java -cp` commands below require a full classpath, including all project dependencies. After running `mvn clean package`, generate the classpath string by running:
        ```bash
        mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
        ```
    *   This will create a `classpath.txt` file in your project root. You will use the content of this file in the commands. The general structure for the classpath argument will be `target/classes:$(cat classpath.txt)` on Linux/macOS or the equivalent on Windows.

## Step 1: Translate Javadoc Comments

This step uses the `AITranslate` tool to find Javadoc comments in the Java source files under `src/main/java/` and translate them in place using the GPT-4o service.

1.  **Navigate to Project Root:** Open your terminal or command prompt and ensure you are in the root directory of the XOTools project.
2.  **Run the Command:**
    *   **Linux/macOS:**
        ```bash
        # Ensure OPENAI_API_KEY (and proxy vars if needed) are set
        # Ensure classpath.txt has been generated
        CP="target/classes:$(cat classpath.txt)"
        java -cp "$CP" api.translate.AITranslate gpt4o src/main/java all
        ```
    *   **Windows (Command Prompt - may require adjustments for `cat`):**
        You might need to first read `classpath.txt` into a variable or directly paste its content.
        ```cmd
        :: Ensure OPENAI_API_KEY (and proxy vars if needed) are set
        :: Ensure classpath.txt has been generated
        :: Example: Manually construct CP or use a tool to get content of classpath.txt
        :: FOR /F "usebackq tokens=*" %i IN (classpath.txt) DO SET CLASSPATH_CONTENT=%i
        :: SET FULL_CP=target\classes;%CLASSPATH_CONTENT%
        :: java -cp "%FULL_CP%" api.translate.AITranslate gpt4o src\main\java all
        
        :: Simpler, if you can paste the classpath directly (ensure it's formatted for Windows with semicolons):
        :: java -cp "target\classes;path\to\dep1.jar;path\to\dep2.jar;..." api.translate.AITranslate gpt4o src\main\java all
        ```
        For Windows, it's often easier to use a shaded JAR if available, or ensure your terminal can correctly expand `$(cat classpath.txt)` (e.g., by using Git Bash or WSL).

    This command uses:
    *   `gpt4o`: Selects the GPT-4o translation service.
    *   `src/main/java`: Specifies the directory to scan for Java files.
    *   `all`: Sets the processing mode to translate all found Javadoc comments (that meet the minimum length criteria).

3.  **Monitor Output:** Check the console for log messages from the tool, which will indicate progress and any errors.

## Step 2: Translate Markdown Files

This step uses the new `MarkdownTranslator` utility to translate the project's Markdown documentation files. For each input file, a new file with the `_zh.md` suffix will be created containing the translated content.

1.  **Navigate to Project Root:** Ensure you are still in the root directory.
2.  **Run the Commands:**
    *   **Linux/macOS (using the same `$CP` variable as in Step 1):**
        ```bash
        # Translate README.md
        java -cp "$CP" utils.MarkdownTranslator README.md README_zh.md

        # Translate GPT4oTranslator_UnitTests_Outline.md
        java -cp "$CP" utils.MarkdownTranslator GPT4oTranslator_UnitTests_Outline.md GPT4oTranslator_UnitTests_Outline_zh.md

        # Translate Single_File_GPT4o_Test_Guide.md
        java -cp "$CP" utils.MarkdownTranslator Single_File_GPT4o_Test_Guide.md Single_File_GPT4o_Test_Guide_zh.md
        ```
    *   **Windows (Command Prompt - using similar classpath setup as in Step 1):**
        ```cmd
        :: Ensure OPENAI_API_KEY (and proxy vars if needed) are set
        :: SET FULL_CP=target\classes;%CLASSPATH_CONTENT% (or manually pasted classpath)

        :: Translate README.md
        :: java -cp "%FULL_CP%" utils.MarkdownTranslator README.md README_zh.md

        :: Translate GPT4oTranslator_UnitTests_Outline.md
        :: java -cp "%FULL_CP%" utils.MarkdownTranslator GPT4oTranslator_UnitTests_Outline.md GPT4oTranslator_UnitTests_Outline_zh.md

        :: Translate Single_File_GPT4o_Test_Guide.md
        :: java -cp "%FULL_CP%" utils.MarkdownTranslator Single_File_GPT4o_Test_Guide.md Single_File_GPT4o_Test_Guide_zh.md
        ```

3.  **Monitor Output:** Observe the console for logs from `MarkdownTranslator`. It will indicate which file is being processed and if any errors occur.

## Next Steps for You

1.  **Execute the Commands:** Please run the commands provided above in your local environment, ensuring all prerequisites are met.
2.  **Review Translations:**
    *   **Javadoc Comments:** Open the Java files within `src/main/java/` (especially those in `api/translate/` and `utils/`) and review the Javadoc comments. Check if they have been translated to Chinese and if the translation quality is acceptable.
    *   **Markdown Files:** Check for the newly created `_zh.md` files (`README_zh.md`, `GPT4oTranslator_UnitTests_Outline_zh.md`, `Single_File_GPT4o_Test_Guide_zh.md`). Review their content for accuracy and completeness of translation.
3.  **Report Back:**
    *   Inform us of the outcome: Were the translations successful?
    *   Report any errors encountered during the process (please provide console logs if possible).
    *   Provide feedback on the quality of the translations.
4.  **Commit and Push (Optional):**
    *   If you are satisfied with the translations, please commit all the modified Java files (with translated Javadocs) and the new `_zh.md` files to your local Git repository.
    *   Then, push these changes to the remote repository.

Your feedback and the translated files are crucial for completing this task. Thank you!
