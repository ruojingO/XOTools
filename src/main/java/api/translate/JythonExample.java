//package api.translate;
//
//import org.python.core.*;
//import org.python.util.PythonInterpreter;
//
//import java.util.Properties;
//
//public class JythonExample {
//    public static void main(String[] args) {
//            // Set the python.home property to the path of the Python environment
//            System.setProperty("python.home", "C:/Users/ruoji/AppData/Local/Programs/Python/Python312");
//
//            PySystemState sys = new PySystemState();
//            // Update the sys.path to include the site-packages directory
//            sys.path.append(new PyString("C:/Users/ruoji/AppData/Local/Packages/PythonSoftwareFoundation.Python.3.12_qbz5n2kfra8p0/LocalCache/local-packages/Python312/site-packages"));
//
//            PythonInterpreter.initialize(System.getProperties(), System.getProperties(), new String[0]);
//            PythonInterpreter i = new PythonInterpreter(null, sys);
//            i.set("apiEng", "hi..i'm english ");
//            // Execute Python script
//            i.execfile("C:/Users/ruoji/Desktop/XWorkSpace/devWorkspace.Backup/XOTools/src/main/java/api/translate/transE.py");
//
//            // Call a function with one parameter
//            PyObject obj = i.get("result", PyObject.class);
//            // Convert the PyObject to a Java String
//            String result = (String) obj.__tojava__(String.class);
//
//            // Rest of your code...
//
//        System.out.println(result);
//
//    }
//}
