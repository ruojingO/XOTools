import org.junit.Test;
import static org.junit.Assert.*;

    public class ContainsTest {

        @Test
        public void testContains() {
            String str = "Hello, world!";
            assertTrue(Contains.contains(str, "world"));
            assertTrue(Contains.contains(str, "Hello"));
            assertTrue(Contains.contains(str, "Hello,world"));
            assertTrue(Contains.contains(str, "Hello，world"));
            assertFalse(Contains.contains(str, "hello"));
            assertFalse(Contains.contains(str, "goodbye"));
            //assertTrue(Contains.contains(str, "，"));
            assertFalse(Contains.contains(str, "，"));
            assertTrue(Contains.contains("My name is 山田太郎", "山田"));
            assertFalse(Contains.contains("Welcome to Sunnyvale", "欢迎"));
        }

    }