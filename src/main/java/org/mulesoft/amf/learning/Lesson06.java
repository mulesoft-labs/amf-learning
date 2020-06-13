package org.mulesoft.amf.learning;


import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.RamlParser;
import amf.client.render.AmfGraphRenderer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.topbraid.jenax.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/*
 * We can create a custom dialect to map our input source to our canonical model, also using the dialect we can validate and do
 * conversion back and forth.
 */
public class Lesson06 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            URL dialectResource = ClassLoader.getSystemResource("dialect/facebook_dialect.raml");
            URL dataResource = ClassLoader.getSystemResource("examples/facebook.raml");

            AMF.registerDialect(dialectResource.toExternalForm()).get();

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(dataResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            System.out.println(document);

            CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(document);
            String jsonLD = jsonLDFuture.get();

            System.out.println("********************");
            System.out.println(jsonLD);

            Model model = JenaUtil.createMemoryModel();
            InputStream inputStream = new ByteArrayInputStream(jsonLD.getBytes(Charset.defaultCharset()));
            model.read(inputStream, document.location(), "JSON-LD");

            System.out.println("********************");
            StmtIterator it = model.listStatements();
            while (it.hasNext()) {
                Statement statement = it.next();

                System.out.println(statement);
            }

            System.out.println("********************");
            System.out.println(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
