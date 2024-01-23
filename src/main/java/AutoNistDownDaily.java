public class AutoNistDownDaily {
    public static void main(String[] args) {
        // auto run daily at 1:00am ,run the command "java -jar nist.jar d:/temp"
        String[] cmd = new String[] { "java", "-jar", "nist.jar", "d:/temp" };
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            // if execute faild, need email to me ,support exchange server and pop3 two
            // protocol
            process.waitFor();
            // system.out.println("AutoNistDownDaily is done ");
            // sleep 10 seconds
            Thread.sleep(10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("AutoNistDownDaily is done  ");

    }
}