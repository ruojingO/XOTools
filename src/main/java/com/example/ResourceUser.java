package com.example;

public class ResourceUser {

    public void useResourceNonCompliant() {
        // Non-compliant: Resource opened via constructor but not closed
        new MyCustomResource("resource1");

        // Non-compliant: Resource opened via factory method but not closed
        MyCustomResource.createResource("resource2");

        // Non-compliant: Resource opened via opening method but not closed
        MyCustomResource resource3 = new MyCustomResource("resource3");
        resource3.initialize();
    }

    public void useResourceCompliant() {
        // Compliant: Resource opened via constructor and closed
        MyCustomResource resource4 = new MyCustomResource("resource4");
        resource4.close();

        // Compliant: Resource opened via factory method and closed
        MyCustomResource resource5 = MyCustomResource.createResource("resource5");
        resource5.close();

        // Compliant: Resource opened via opening method and closed
        MyCustomResource resource6 = new MyCustomResource("resource6");
        resource6.initialize();
        resource6.close();
    }
}
