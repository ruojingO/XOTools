import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubToText {
    public static void main(String[] args) {
        String epubFilePath = "path/to/your/epub/file.epub";
        try {
            FileInputStream epubInputStream = new FileInputStream(epubFilePath);
            EpubReader epubReader = new EpubReader();
            Book book = epubReader.readEpub(epubInputStream);

            List<Resource> resources = book.getResources().getAll();
            for (Resource resource : resources) {
                if (resource.getMediaType().getName().contains("html")) {
                    String htmlContent = new String(resource.getData(), resource.getInputEncoding());
                    String textContent = htmlToText(htmlContent);
                    System.out.println(textContent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String htmlToText(String html) {
        return html.replaceAll("(?s)<[^>]*>(\\s*<>]*>)*", " ").replaceAll("\\s+", " ").trim();
    }
}
