//package api.translate;
//
//import org.python.util.*;
//
//import java.util.*;
//
//public class CallPythonWithJython {
//    public static void main(String[] args) throws Exception {
//        // Set up properties for Jython
//        Properties props = new Properties();
//        props.put("python.home", "C:\\Users\\ruoji\\AppData\\Local\\Programs\\Python");
//        props.put("python.console.encoding", "UTF-8");
//        props.put("python.security.respectJavaAccessibility", "false");
//        props.put("python.import.site", "false");
//        Properties preprops = System.getProperties();
//
//        // Initialize Python interpreter
//        PythonInterpreter.initialize(preprops, props, new String[0]);
//        PythonInterpreter interpreter = new PythonInterpreter();
//
//        interpreter.exec("import  google.generativeai as genai");
//        interpreter.exec("apikey = 'AIzaSyDUfmAheoLC2iG6Mw3snhi_hXFwmq7aA4I'");
//        interpreter.exec("model = genai.GenerativeModel('gemini-pro')");
//
//        // Your Python code using the client object
//        String jdoc ="hi.this jdk doc,can u help translate?";
//        //interpreter.exec("response = client.query(query='pls translate the java.jdk.javadoc english to chinese,the javadoc is"+jdoc+" ')");
//
//        // Access results from Java
//        String responseText = interpreter.get("response.text").toString();
//        System.out.println(responseText);
//    }
//}
