package org.mulesoft.amf.learning;


import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.RamlParser;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

/*
 * Parse json
 */
public class Lesson12 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            URL dialectResource = ClassLoader.getSystemResource("dialect/tokenizer_single_dialect.yaml");
            URL dataResource = ClassLoader.getSystemResource("examples/tokenizer_hierarchy.json");

            AMF.registerDialect(dialectResource.toExternalForm()).get();

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(dataResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            System.out.println(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
