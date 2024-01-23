public class Contains {


    public static boolean contains(String str, String subStr) {
        // Split the subStr by commas and trim each substring
        String[] subStrs = subStr.split("[，,]");
        for (String sub : subStrs) {
            sub = sub.trim();
            if (str.contains(sub)) {
                return true;
            }
        }
        return false;
    }
}


