package org.mulesoft.amf.learning;

import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.RamlParser;
import amf.client.render.Raml10Renderer;

import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * AMF has a WebApi model which represents canonical model for APIs an allow us to modify it in a generic way
 */
public class Lesson03 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            URL systemResource = ClassLoader.getSystemResource("api/library.raml");

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());
            Document document = (Document) parseFileAsync.get();

            WebApi domainElement = (WebApi) document.encodes();

            domainElement.withSchemes(Arrays.asList("HTTP"));
            domainElement.withDocumentationTitle("New Home");
            domainElement.withDocumentationUrl("http://example.com/mutator.raml");

            System.out.println("********************");
            CompletableFuture<String> ramlV2Future = new Raml10Renderer().generateString(document);
            System.out.println(ramlV2Future.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
