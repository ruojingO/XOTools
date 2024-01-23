package ai;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileSplitter {
    public static void main(String[] args) {
        String inputFile = "C:\\Users\\ruoji\\Documents\\BaiduSyncdisk\\XDhamma\\original\\B.sumana.txt"; // Replace with your file path
        long maxFileSize = (long)(0.8* 1024 * 1024); // Size in bytes (1MB in this example)
        System.out.println(maxFileSize);
        splitFile(inputFile, maxFileSize);
    }

    public static void splitFile(String inputFile, long maxFileSize) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(inputFile), StandardCharsets.UTF_8))) {

            int filePart = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                File newFile = new File(inputFile + ".part" +   String.format("%02d", filePart) + ".txt");
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(newFile), StandardCharsets.UTF_8))) {

                    long fileSize = 0;
                    do {
                        writer.write(line + "\n");
                        fileSize += line.getBytes(StandardCharsets.UTF_8).length;
                        line = reader.readLine();
                    } while (line != null && fileSize < maxFileSize);

                    filePart++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
