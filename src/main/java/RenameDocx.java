


//Here is an example Java program that
//        uses Apache POI to batch rename all docx files in the current directory to the first line of text in the Word document:
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.common.io.Files;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
public class RenameDocx {
  public static void main(String[] args) throws IOException, InvalidFormatException {
    //"C:/Users/ruojing/Desktop/ttt/stt/"
    String path = args[0].replace('\\','/');
    if(!path.endsWith("/")){
      path+="/";
    }
    File dir = new File(path);
    File ft = new File(path+"/trans");
    if(!ft.exists()){
      ft.mkdir();
    }

    File[] files = dir.listFiles((d, name) -> name.endsWith(".docx"));
    for (File file : files) {
      XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
      XWPFParagraph firstParagraph = doc.getParagraphs().get(0);
      String newName = firstParagraph.getText();
      System.out.println(file.getName()+"---"+newName);
      File newFile = new File(dir+"/trans/", newName + ".docx");
      //if(!file.getName().equals(newFile.getName())){
        Files.copy(file,newFile);
      //}
      //boolean ok = file.renameTo(newFile);
       System.out.println("rename ok");
      doc.close();
    }
  }
}



//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//public class RenameDocx {
//    public static void main(String[] args) throws IOException {
//// Set the directory path
//        String directoryPath = "C:/Users/ruojing/Desktop/ttt/经文/经文/";
//
//// Create a File object to represent the directory
//        File directory = new File(directoryPath);
//
//// List of all files in the directory
//        String[] fileList = directory.list();
//
//        for (String fileName : fileList) {
//            if (fileName.endsWith(".docx")) {
//                Path path = Paths.get(directoryPath + fileName);
//                byte[] data = Files.readAllBytes(path);
//
//                FileInputStream fis = new FileInputStream(directoryPath+fileName);
//                String firstLine = getFirstLine(fis);
//              System.out.println(">>"+fileName);
//                System.out.println(firstLine);
//                fis.close();
//
//                FileOutputStream fos = new FileOutputStream(firstLine + ".docx");
//                fos.write(data);
//                fos.close();
//            }
//        }
//
//    }
//
//    public static String getFirstLine(FileInputStream fis) throws IOException {
//        String firstLine = "";
//        int content;
//        while ((content = fis.read()) != -1) {
//            if (content == 10) {
//                break;
//            }
//            firstLine += (char) content;
//        }
//        return firstLine;
//
//    }
//}