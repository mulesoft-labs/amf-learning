package org.mulesoft.amf.learning;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import amf.Core;
import amf.client.parse.RamlParser;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;

/*
 * AMF has a RAML Parser
 */
public class Lesson01 {
    public static void main(String[] args) {
        try {
            Core.init().get();

            URL systemResource = ClassLoader.getSystemResource("api/library.raml");

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());
            Document document = (Document) parseFileAsync.get();

            System.out.println(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
