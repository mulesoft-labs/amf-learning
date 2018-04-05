package org.mulesoft.amf.learning;


import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.RamlParser;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

/*
 * We can register multiples dialects
 */
public class Lesson07 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            URL twitterDialectResource = ClassLoader.getSystemResource("dialect/twitter_dialect.raml");
            URL facebookDialectResource = ClassLoader.getSystemResource("dialect/facebook_dialect.raml");
            URL instagramDialectResource = ClassLoader.getSystemResource("dialect/instagram_dialect.raml");

            URL twitterDataResource = ClassLoader.getSystemResource("examples/twitter.raml");
            URL facebookDataResource = ClassLoader.getSystemResource("examples/facebook.raml");
            URL instagramDataResource = ClassLoader.getSystemResource("examples/instagram.raml");

            AMF.registerDialect(twitterDialectResource.toExternalForm()).get();
            AMF.registerDialect(facebookDialectResource.toExternalForm()).get();
            AMF.registerDialect(instagramDialectResource.toExternalForm()).get();

            RamlParser parser = new RamlParser();

            CompletableFuture<BaseUnit> parseTwitterFileAsync = parser.parseFileAsync(twitterDataResource.toExternalForm());
            CompletableFuture<BaseUnit> parseFacebookFileAsync = parser.parseFileAsync(facebookDataResource.toExternalForm());
            CompletableFuture<BaseUnit> parseInstagramFileAsync = parser.parseFileAsync(instagramDataResource.toExternalForm());

            BaseUnit twitterDocument = parseTwitterFileAsync.get();
            BaseUnit facebookDocument = parseFacebookFileAsync.get();
            BaseUnit instagramDocument = parseInstagramFileAsync.get();

            System.out.println(twitterDocument);
            System.out.println(facebookDocument);
            System.out.println(instagramDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
