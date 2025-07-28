package bcJdkSupport;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

/**
 * A simple class to demonstrate the compatibility of Bouncy Castle with JDK 8+.
 */
public class BcJdk8Compatibility {

    public static void main(String[] args) {
        // Add Bouncy Castle as a security provider
        Security.addProvider(new BouncyCastleProvider());

        // Check if the provider is successfully added
        if (Security.getProvider("BC") != null) {
            System.out.println("Bouncy Castle provider added successfully.");
            System.out.println("This demonstrates that bcprov-jdk18on is compatible with the current JDK.");
        } else {
            System.out.println("Failed to add Bouncy Castle provider.");
        }
    }
}
