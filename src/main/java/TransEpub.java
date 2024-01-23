//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import nl.siegmann.epublib.domain.Book;
//import nl.siegmann.epublib.epub.EpubReader;
//import org.apache.commons.io.IOUtils;
//
//public class TransEpub {
//    public static void main(String[] args) throws IOException {
//        // Load the EPUB file
//        File epubFile = new File("mybook.epub");
//        InputStream epubInputStream = new FileInputStream(epubFile);
//        Book book = (new EpubReader()).readEpub(epubInputStream);
//
//        // Extract the text content from the book
//        String text = book.getContents().stream()
//                .map(c -> {
//                    try {
//                        return IOUtils.toString(c.getReader());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .reduce("", (a, b) -> a + "\n" + b);
//
//        // Write the text content to a file
//        File txtFile = new File("mybook.txt");
//        OutputStream txtOutputStream = new FileOutputStream(txtFile);
//        IOUtils.write(text, txtOutputStream, "UTF-8");
//    }
//}
