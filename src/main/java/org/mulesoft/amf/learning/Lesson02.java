package org.mulesoft.amf.learning;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import amf.Core;
import amf.client.parse.RamlParser;
import amf.client.model.document.BaseUnit;

/*
 * After using AMF RAML parser you get a document which could be represented in different mediaType
 */
public class Lesson02 {
    public static void main(String[] args) {
        try {
            Core.init().get();

            URL systemResource = ClassLoader.getSystemResource("api/library.raml");

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            System.out.println("********************");
            CompletableFuture<String> ramlV1Future = Core.generator("RAML 0.8", "application/yaml").generateString(document);
            System.out.println(ramlV1Future.get());

            System.out.println("********************");
            CompletableFuture<String> ramlV2Future = Core.generator("RAML 1.0", "application/yaml").generateString(document);
            System.out.println(ramlV2Future.get());

            System.out.println("********************");
            CompletableFuture<String> oasFuture = Core.generator("OAS 2.0", "application/json").generateString(document);
            System.out.println(oasFuture);

            System.out.println("********************");
            CompletableFuture<String> jsonLDFuture = Core.generator("AMF Graph", "application/ld+json").generateString(document);
            System.out.println(jsonLDFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
