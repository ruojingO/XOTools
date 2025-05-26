import java.text.SimpleDateFormat;

public class Test {
    private static final SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        try {
            int result = 1 / 0; // This will throw an exception
        } catch (ArithmeticException e) {
            System.out.println("Error: Division by zero is not allowed.");
        }

        // Example usage of the SimpleDateFormat (optional)
        System.out.println("Current date format: " + sd.format(new java.util.Date()));
    }
}
