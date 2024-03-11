package api.translate;

import org.apache.commons.lang3.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class JavadocTranslator {
    public static void main(String[] args) throws IOException, InterruptedException {
        String filePath = "C:\\Users\\ruoji\\Desktop\\tempWorkspace\\Object.java";
        coreTrans(new File(filePath));

    }

    public static void coreTrans(File file) throws IOException, InterruptedException {
        Path filePath= file.toPath();
        String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        // Pattern to match Javadoc comments
        Pattern javadocPattern = Pattern.compile("/\\*\\*.*?\\*/", Pattern.DOTALL);
        Matcher javadocMatcher = javadocPattern.matcher(fileContent);

        StringBuilder newFileContent = new StringBuilder(fileContent);

        // Offset to keep track of replacements within the StringBuilder
        int offset = 0;

        while (javadocMatcher.find()) {
            String javadoc = javadocMatcher.group();
            String translatedJavadoc =javadoc;
            if(javadoc.length()>200) {
                //System.out.println(javadoc);
                String prompt = "pls translate to Chinese:" + PythonExecutor.escapeDoubleQuotes(javadoc);
                // Translate the Javadoc comment
                System.out.println(prompt);
                translatedJavadoc = PythonExecutor.geminiTrans(prompt);
                Random r = new Random();
                int sleepTime = 100 + r.nextInt(3) * 100;
                Thread.sleep(sleepTime);
            }
            // Replace the original Javadoc with the translated one in the StringBuilder
            newFileContent.replace(javadocMatcher.start() + offset, javadocMatcher.end() + offset, translatedJavadoc);

            // Update the offset based on the length difference between the original and translated Javadoc
            offset += translatedJavadoc.length() - javadoc.length();
        }
        //System.out.println("finish trans :"+ filePath);
        // Write the new content to the same file or a new file
        Files.write(Paths.get(filePath.toString()), newFileContent.toString().getBytes("utf-8"));
        System.out.println(" reWrite ok");
    }
}
