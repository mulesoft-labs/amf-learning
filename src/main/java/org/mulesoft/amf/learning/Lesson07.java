package org.mulesoft.amf.learning;


import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.Aml10Parser;
import amf.client.parse.RamlParser;
import amf.client.render.AmfGraphRenderer;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.topbraid.jenax.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

            Aml10Parser parser = new Aml10Parser();

            CompletableFuture<BaseUnit> parseTwitterFileAsync = parser.parseFileAsync(twitterDataResource.toExternalForm());
            CompletableFuture<BaseUnit> parseFacebookFileAsync = parser.parseFileAsync(facebookDataResource.toExternalForm());
            CompletableFuture<BaseUnit> parseInstagramFileAsync = parser.parseFileAsync(instagramDataResource.toExternalForm());

            BaseUnit twitterDocument = parseTwitterFileAsync.get();
            BaseUnit facebookDocument = parseFacebookFileAsync.get();
            BaseUnit instagramDocument = parseInstagramFileAsync.get();

            String twitterJsonLD = printJsonLD("Twitter", twitterDocument);
            printJsonLD("Facebook", facebookDocument);
            printJsonLD("Instagram", instagramDocument);

            Model model = JenaUtil.createMemoryModel();
            InputStream inputStream = new ByteArrayInputStream(twitterJsonLD.getBytes(Charset.defaultCharset()));
            model.read(inputStream, twitterDocument.location(), "JSON-LD");

            System.out.println("Query Assets");
            InputStream assetsQueryIS = ClassLoader.getSystemResourceAsStream("queries/person.sparql");
            String assetsQuery = IOUtils.toString(assetsQueryIS, Charset.defaultCharset());
            Query jenaAssetsQuery = QueryFactory.create(assetsQuery);


            try (QueryExecution execution = QueryExecutionFactory.create(jenaAssetsQuery, model)) {
                ResultSet rs = execution.execSelect();

                String results = ResultSetFormatter.asText(rs);
                System.out.println(results);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String printJsonLD(String name, BaseUnit baseUnit) throws ExecutionException, InterruptedException {
        System.out.println(name + " JSON-LD");

        CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(baseUnit);
        String jsonLD = jsonLDFuture.get();

        System.out.println("****************");
        System.out.println(jsonLD);
        System.out.println("****************");

        return jsonLD;
    }
}
