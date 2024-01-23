import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 此类提供使用Jsoup库将EPUB文件转换为纯文本的功能。
 */
public class EpubToTextWithJsoup {
    private java.util.Calendar c = java.util.Calendar.getInstance();

    /**
     * 读取文件长度
     */
    public static Long readFileLength(String fileName) {
        Long fileLength = 0L;
        try {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(new File(fileName), "r")) {
                fileLength = randomAccessFile.length();

            }
        } catch (Exception e) {
            // logger.error(e.getMessage(), e);
        }
        return fileLength;
    }

    /**
     * 主函数
     */
    public static void main(String[] args) throws IOException {
        List<String> ll = new ArrayList<>();
        String epubFilePath = "C:\\Users\\ruoji\\Desktop\\S.epub";
        String outputFilePath = "C:\\Users\\ruoji\\Documents\\BaiduSyncdisk\\XDhamma\\original\\B.sumana.txt";
        File epubFile = new File(epubFilePath);
        File tempDirectory = new File("C:\\temp\\epubTemp");

        File fo = new File(outputFilePath);
        if (fo.exists()) {
            FileUtils.forceDelete(fo);
        }
        // 解压EPUB文件
        try (InputStream inputStream = new FileInputStream(epubFile);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;

            System.out.println("Unzipping EPUB file...");
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                File entryFile = new File(tempDirectory, entryName);

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    entryFile.getParentFile().mkdirs();

                    try (OutputStream fos = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[1024];
                        int len;

                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
            System.out.println("EPUB file unzipped.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 从XHTML文件中提取文本内容并保存到outputFilePath
        try {
            System.out.println("Processing XHTML files...");
            Files.walk(tempDirectory.toPath())
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".xhtml")
                            || p.toString().toLowerCase().endsWith(".html"))
                    .forEach(p -> extractTextFromXHTML(p, new File(outputFilePath)));
            System.out.println("Text extraction is complete. Check the output file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (epubFile.exists()) {
            FileUtils.forceDelete(epubFile);
        }
    }

    /**
     * 从XHTML文件中提取文本内容
     */
    static void extractTextFromXHTML(Path inputPath, File outputFile) {
        try {
            Document doc = Jsoup.parse(inputPath.toFile(), "UTF-8");
            try (FileWriter writer = new FileWriter(outputFile, true)) {
                // Your code here
                String text = doc.text().trim();
                int maxLineLength = 50;
                int startIndex = 0;

                while (startIndex < text.length()) {
                    int endIndex = Math.min(startIndex + maxLineLength, text.length());
                    while (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
                        endIndex++;
                    }
                    writer.append(text.substring(startIndex, endIndex).trim());
                    writer.append("\n");
                    while (endIndex < text.length() && Character.isWhitespace(text.charAt(endIndex))) {
                        endIndex++;
                    }
                    startIndex = endIndex;
                }
                writer.append("\n\n");

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}