package api.translate;

import org.slf4j.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

public class AITranslate {
    private static final Logger LOGGER = LoggerFactory.getLogger(AITranslate.class);
    private static final List<Pattern> EXCLUDED_DIRECTORIES_PATTERNS = new ArrayList<Pattern>();
    private static int totalFiles = 0;
    private static int totalCommentsLength = 0;

    public static void main(String[] args) throws IOException {
        // 初始化排除的目录模式，这些模式可以从配置文件或命令行参数中动态读取
        initializeExcludedDirectories(Arrays.asList("com/sun/.*", "org/omg/.*","java/applet.*","java/awt.*","javax/swing.*","javax/sound.*","javax/print.*")); // 示例：添加需要排除的模式

        File rootDir = new File("C:\\Users\\ruoji\\Desktop\\tempWorkspace\\jdk1.8-211\\src"); // 替换为你的根目录路径
        traverseDirectory(rootDir);
        LOGGER.info("Finished processing all files. Total comments length: {}", totalCommentsLength);
    }

    private static void initializeExcludedDirectories(List<String> patterns) {
        for (String pattern : patterns) {
            EXCLUDED_DIRECTORIES_PATTERNS.add(Pattern.compile(pattern));
        }
    }

    private static int javaFileCounter = 0; // Counter for the number of Java files
    private static void traverseDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !isExcludedDirectory(file)) {
                    traverseDirectory(file);
                } else if (file.getName().endsWith(".java")) {
                   dealTransError(file);
                    //deal2019(file);
                   //transByPatch(file);
                }
        }
    }
}

    private static void transByPatch(File file) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        LocalDate modifiedDate = attrs.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (modifiedDate.getYear() == 2024 && modifiedDate.getMonthValue()==1 &&modifiedDate.getDayOfMonth()==12) {
            javaFileCounter++; // Increment the counter
            System.out.println("Translating File #" + javaFileCounter + ": " + file.toPath());
            try {
                JavadocTranslator.coreTrans(file);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void dealTransError(File file) throws IOException {
        Path filePath= file.toPath();
        String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        System.out.println(filePath);
        //debug_error_string =
        if(fileContent.indexOf("Traceback (most recent call last):")!=-1){
            String xfp= filePath.toString().replace("\\src\\","\\src-eng\\");
            System.out.println(filePath);
            System.out.println(xfp);
            javaFileCounter++; // Increment the counter
            System.out.println("Translating File #" + javaFileCounter + ": " + file.toPath());
            try {
                //JavadocTranslator.coreTrans(new File(xfp));
                String nfileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                Files.write(Paths.get(filePath.toString()), nfileContent.toString().getBytes("utf-8"));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private static void deal2019(File file) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        LocalDate modifiedDate = attrs.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (modifiedDate.getYear() == 2019) {
            javaFileCounter++; // Increment the counter
            System.out.println("Translating File #" + javaFileCounter + ": " + file.toPath());
            //if(file.getName().equals("CachedRowSet.java")) continue;
            try {
                JavadocTranslator.coreTrans(file);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //
//    private static boolean isExcludedDirectory(File dir) {
//        String path = dir.getPath().replace('\\', '/');
//        for (Pattern pattern : EXCLUDED_DIRECTORIES_PATTERNS) {
//            if (pattern.matcher(path).matches()) {
//                return true;
//            }
//        }
//        return false;
//    }
    private static boolean isExcludedDirectory(File dir) {
        String path = dir.getPath().replace('\\', '/');
        for (Pattern pattern : EXCLUDED_DIRECTORIES_PATTERNS) {
            if (pattern.matcher(path).find()) { // Use find() instead of matches() to check if any part of the path matches the pattern
                return true;
            }
        }
        return false;
    }


}

