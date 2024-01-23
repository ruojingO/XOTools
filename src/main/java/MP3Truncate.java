import java.io.IOException;

public class MP3Truncate {

    public static void main(String[] args) throws IOException, InterruptedException {
        MP3Truncate mp3Truncate = new MP3Truncate();
        mp3Truncate.truncateMp3("input.mp3", "output.mp3", "00:00:30", "00:01:30");
    }

    public void truncateMp3(String inputFile, String outputFile, String startTime, String endTime) throws IOException, InterruptedException {
        // Create the ffmpeg command
        String[] command = new String[]{
            "ffmpeg",
            "-i", inputFile,
            "-ss", startTime,
            "-to", endTime,
            "-c", "copy",
            outputFile
        };

        // Execute the command using ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        process.waitFor();
    }
}
