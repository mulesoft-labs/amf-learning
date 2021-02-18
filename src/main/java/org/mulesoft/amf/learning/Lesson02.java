package org.mulesoft.amf.learning;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import amf.Core;
import amf.client.AMF;
import amf.client.parse.RamlParser;
import amf.client.model.document.BaseUnit;
import amf.client.render.*;

/*
 * After using AMF RAML parser you get a document which could be represented in different mediaType
 */
public class Lesson02 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            URL systemResource = ClassLoader.getSystemResource("api/library.raml");

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            System.out.println("******* RAML 0.8 *******");
            CompletableFuture<String> ramlV1Future = new Raml08Renderer().generateString(document);
            System.out.println(ramlV1Future.get());

            System.out.println("******* RAML 1.0 *******");
            CompletableFuture<String> ramlV2Future = new Raml10Renderer().generateString(document);
            System.out.println(ramlV2Future.get());

            System.out.println("******* OAS *******");
            CompletableFuture<String> oasFuture = new Oas20Renderer().generateString(document);
            System.out.println(oasFuture.get());

            System.out.println("******* AsyncAPI 2.0 *******");
            CompletableFuture<String> asyncApiFuture = new Async20Renderer().generateString(document);
            System.out.println(asyncApiFuture.get());

            System.out.println("******* Graph *******");
            CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(document);
            System.out.println(jsonLDFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
