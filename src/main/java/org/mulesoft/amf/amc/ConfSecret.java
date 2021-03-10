package org.mulesoft.amf.amc;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConfSecret {

    private final Resource subject;
    private final Resource property;
    private final String value;

    private ConfSecret(Resource subject, Resource property, String value) {
        this.subject = subject;
        this.property = property;
        this.value = value;
    }

    public ConfSecret encrypted() {
        // Invoke secret manager and encrypt :)
        final String encrypted = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return new ConfSecret(subject, property, encrypted);
    }

    public static ConfSecret create(QuerySolution solution) {
        return new ConfSecret(
                solution.getResource("subject"),
                solution.getResource("property"),
                solution.getLiteral("value").getString());
    }

    public static ConfSecret create(String subject, String property, String value) {
        return new ConfSecret(
                ResourceFactory.createResource(subject),
                ResourceFactory.createResource(property),
                value);
    }

    String getSubject() { return subject.getURI(); }

    String getProperty() { return property.getURI(); }

    String getValue() { return value; }
}
