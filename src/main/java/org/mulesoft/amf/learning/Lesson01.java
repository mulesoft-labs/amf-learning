package org.mulesoft.amf.learning;

import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.parse.*;
import amf.core.remote.*;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

/*
 * AMF has a RAML, OAS 2.0/3.0, and AsyncAPI parsers
 */
public class Lesson01 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            // RAML parser
            URL systemResource = ClassLoader.getSystemResource("api/library.raml");

            Parser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());
            Document document = (Document) parseFileAsync.get();

            System.out.println(document);

            // OAS 3.0 parser
            parser = new Oas30Parser();
            parseFileAsync = parser.parseFileAsync("https://raw.githubusercontent.com/OAI/OpenAPI-Specification/3.1.0/examples/v3.0/api-with-examples.json");
            document = (Document) parseFileAsync.get();

            System.out.println(document);

            // AsyncAPI 2.0 parser
            parser = new Async20Parser();
            parseFileAsync = parser.parseFileAsync("https://raw.githubusercontent.com/asyncapi/asyncapi/2.0.0/examples/2.0.0/streetlights.yml");
            document = (Document) parseFileAsync.get();

            System.out.println(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
