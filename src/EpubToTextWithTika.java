import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EpubToTextWithTika {
    public static void main(String[] args) {
        String epubFilePath = "path/to/your/epub/file.epub";
        String outputFilePath = "C:\\temp\\trans.txt";
        File epubFile = new File(epubFilePath);

        try (InputStream inputStream = new FileInputStream(epubFile)) {
            // Instantiate the EPUB Parser
            Parser parser = new EpubParser();
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            // Parse the EPUB file
            parser.parse(inputStream, handler, metadata, parseContext);
            String extractedText = handler.toString();

            // Save the extracted text to a local file
            Files.write(Paths.get(outputFilePath), extractedText.getBytes());

        } catch (IOException | SAXException | TikaException e) {
            e.printStackTrace();
        }
    }
}
