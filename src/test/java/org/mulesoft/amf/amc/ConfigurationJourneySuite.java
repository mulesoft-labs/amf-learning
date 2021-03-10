package org.mulesoft.amf.amc;

import amf.client.model.document.DialectInstance;
import amf.plugins.features.validation.JenaRdfModel;
import org.apache.jena.query.QuerySolution;
import org.junit.Test;
import org.mulesoft.amf.util.Instance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;


public class ConfigurationJourneySuite {

    @Test public void testListSecrets() throws ExecutionException, InterruptedException {
        final ConfigurationJourney journey = new ConfigurationJourney();
        final Stream<ConfSecret> secrets = journey.listSecrets(AGENT5).get();
        final List<String> actual = secrets.map(ConfSecret::encrypted).map(ConfSecret::getValue).collect(Collectors.toList());
        final List<String> expected = Arrays.asList("ZHJvd3NzYXAtdGVyY2Vz", "c2VjcmV0LXBhc3N3b3Jk");
         assertEquals(expected, actual);
    }

    @Test public void testUpdateSecrets() throws ExecutionException, InterruptedException {
        final ConfigurationJourney journey = new ConfigurationJourney();
        final Stream<ConfSecret> encrypted = journey.listSecrets(AGENT5).get().map(ConfSecret::encrypted);
        final DialectInstance instance = journey.updateSecrets(AGENT5, encrypted).get();
        final String yaml = journey.renderAml(instance, "application/json").get();
        assertTrue(yaml.contains("\"keyStorePassword\": \"c2VjcmV0LXBhc3N3b3Jk\""));
        assertTrue(yaml.contains("\"keyStoreAliasPassword\": \"ZHJvd3NzYXAtdGVyY2Vz\""));
    }

    private final static Instance AGENT5 = Instance.create("examples/agent5.json", "Mule Application 0.1");

}
