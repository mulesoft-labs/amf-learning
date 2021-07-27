package org.mulesoft.amf.learning;

import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.Api;
import amf.client.model.domain.EndPoint;
import amf.client.parse.*;
import amf.client.render.*;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * AMF has a WebApi model which represents canonical model for APIs an allow us to modify it in a generic way
 */
public class Lesson03 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            // List of triples with a parser, renderer and input URL for RAML, OAS and AsyncAPI
            List<Object[]> inputCases = Arrays.asList(new Object[][] {
              { new RamlParser(), new Raml10Renderer(), ClassLoader.getSystemResource("api/library.raml").toExternalForm() },
              { new Oas30Parser(), new Oas30Renderer(), "https://raw.githubusercontent.com/OAI/OpenAPI-Specification/3.1.0/examples/v3.0/api-with-examples.json" },
              { new Async20Parser(), new Async20Renderer(), "https://raw.githubusercontent.com/asyncapi/asyncapi/2.0.0/examples/2.0.0/streetlights.yml" }
            });

            // For all input format cases, homogeneously add an Endpoint
            for (Object[] inputCase : inputCases) {
                Parser parser = (Parser) inputCase[0];
                Renderer renderer = (Renderer) inputCase[1];
                String inputUrl = (String) inputCase[2];

                CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(inputUrl);

                Document document = (Document) parseFileAsync.get();

                Api<EndPoint> domainElement = (Api<EndPoint>) document.encodes();

                domainElement.withEndPoint("newEndpoint")
                  .withDescription("Example endpoint to demonstrate canonical model modifications");

                System.out.println("***********************");
                CompletableFuture<String> renderFuture = renderer.generateString(document);
                System.out.println(renderFuture.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
