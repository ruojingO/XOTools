package api.translate;

import org.apache.commons.lang3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class PythonExecutor {
    public static String geminiTrans(String transE) {
        String pythonScriptPath = "C:/Users/ruoji/Desktop/XWorkSpace/devWorkspace.Backup/XOTools/src/main/java/api/translate/transE.py";
        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, transE);
        processBuilder.redirectErrorStream(true);
        Map<String, String> env = processBuilder.environment();

        //todo from env get
        env.put("GOOGLE_GENAI_API_KEY", "AIzaSyDUfmAheoLC2iG6Mw3snhi_hXFwmq7aA4I");
        String completeString = "";
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
            completeString = result.toString();
            int exitCode = process.waitFor();

            if (isTransFailed(completeString)) {
//                System.err.println("Translation failed,will retry 3 times. for: " + transE);
//                for (int i = 0; i < 3; i++) { // Retry up to 3 times
//                    Thread.sleep(3000); // Wait for 3 seconds
//                    completeString = geminiTrans(transE); // Recursive call to retry translation
//                    if (!isTransFailed(completeString)) {
//                        break; // Break the loop if successful
//                    }
//               }
               // if(isTransFailed(completeString)){
                    System.err.println(completeString);
                    System.out.println("***unPredict error,will stop this text's translation,return original text:"+transE);
                    return transE;
                //}
        }
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
    return completeString;
}

    private static boolean isTransFailed(String completeString) {
        return completeString.contains("Traceback (most recent call last):");
    }

    public static String escapeDoubleQuotes(String input) {
        return input.replace("\"", "\\\"");
    }

    public static void main(String[] args) {
        String ps=" pls translate to Chinese:";
        String test= StringEscapeUtils.escapeJava(ps + "/**\n" +
                "     * Indicates whether some other object is \"equal to\" this one.\n" +
                "     * <p>\n" +
                "     * The {@code equals} method implements an equivalence relation\n" +
                "     * on non-null object references:\n" +
                "     * <ul>\n" +
                "     * <li>It is <i>reflexive</i>: for any non-null reference value\n" +
                "     *     {@code x}, {@code x.equals(x)} should return\n" +
                "     *     {@code true}.\n" +
                "     * <li>It is <i>symmetric</i>: for any non-null reference values\n" +
                "     *     {@code x} and {@code y}, {@code x.equals(y)}\n" +
                "     *     should return {@code true} if and only if\n" +
                "     *     {@code y.equals(x)} returns {@code true}.\n" +
                "     * <li>It is <i>transitive</i>: for any non-null reference values\n" +
                "     *     {@code x}, {@code y}, and {@code z}, if\n" +
                "     *     {@code x.equals(y)} returns {@code true} and\n" +
                "     *     {@code y.equals(z)} returns {@code true}, then\n" +
                "     *     {@code x.equals(z)} should return {@code true}.\n" +
                "     * <li>It is <i>consistent</i>: for any non-null reference values\n" +
                "     *     {@code x} and {@code y}, multiple invocations of\n" +
                "     *     {@code x.equals(y)} consistently return {@code true}\n" +
                "     *     or consistently return {@code false}, provided no\n" +
                "     *     information used in {@code equals} comparisons on the\n" +
                "     *     objects is modified.\n" +
                "     * <li>For any non-null reference value {@code x},\n" +
                "     *     {@code x.equals(null)} should return {@code false}.\n" +
                "     * </ul>\n" +
                "     * <p>\n" +
                "     * The {@code equals} method for class {@code Object} implements\n" +
                "     * the most discriminating possible equivalence relation on objects;\n" +
                "     * that is, for any non-null reference values {@code x} and\n" +
                "     * {@code y}, this method returns {@code true} if and only\n" +
                "     * if {@code x} and {@code y} refer to the same object\n" +
                "     * ({@code x == y} has the value {@code true}).\n" +
                "     * <p>\n" +
                "     * Note that it is generally necessary to override the {@code hashCode}\n" +
                "     * method whenever this method is overridden, so as to maintain the\n" +
                "     * general contract for the {@code hashCode} method, which states\n" +
                "     * that equal objects must have equal hash codes.\n" +
                "     *\n" +
                "     * @param   obj   the reference object with which to compare.\n" +
                "     * @return  {@code true} if this object is the same as the obj\n" +
                "     *          argument; {@code false} otherwise.\n" +
                "     * @see     #hashCode()\n" +
                "     * @see     java.util.HashMap\n" +
                "     */");
        geminiTrans(test);
    }
}
