import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSync {
    public static void main(String[] args) throws IOException {
        Path sourceDir = Paths.get("C:/Users/ruoji/cursor-tutor/Code/");
        Path destDir = Paths.get("C:/Users/ruoji/Documents/BaiduSyncdisk/XWorkSpace/devWorkspace.Backup/XOTools/src/main/java/");

        System.out.println("syn file doing ...");
        // Synchronize files from source to destination
        Files.walk(sourceDir)
             .filter(Files::isRegularFile)
             .forEach(sourceFile -> {
                 Path destFile = destDir.resolve(sourceDir.relativize(sourceFile));
                 try {
                     Files.createDirectories(destFile.getParent());
                     Files.copy(sourceFile, destFile);
                 } catch (IOException e) {
                     System.err.println("Failed to copy " + sourceFile + " to " + destFile + ": " + e);
                 }
             });

        // Synchronize files from destination to source
        Files.walk(destDir)
             .filter(Files::isRegularFile)
             .forEach(destFile -> {
                 Path sourceFile = sourceDir.resolve(destDir.relativize(destFile));
                 try {
                     if (!Files.exists(sourceFile) || Files.getLastModifiedTime(destFile).compareTo(Files.getLastModifiedTime(sourceFile)) > 0) {
                         try {
                             Files.createDirectories(sourceFile.getParent());
                             Files.copy(destFile, sourceFile);
                         } catch (IOException e) {
                             System.err.println("Failed to copy " + destFile + " to " + sourceFile + ": " + e);
                         }
                     }
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             });
    }
}
