package org.mulesoft.amf.learning;

import amf.Core;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.parse.RamlParser;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.topbraid.spin.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * Based on our document we can serialize it as JSON-LD and then use SPARQL to run some queries.
 */
public class Lesson05 {
    public static void main(String[] args) {
        try {
            Core.init().get();

            URL systemResource = ClassLoader.getSystemResource("api/library.raml");
            InputStream sparqlInputStream = ClassLoader.getSystemResourceAsStream("queries/endpoints.sparql");

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());
            Document document = (Document) parseFileAsync.get();

            CompletableFuture<String> jsonLDFuture = Core.generator("AMF Graph", "application/ld+json").generateString(document);
            String jsonLD = jsonLDFuture.get();
            System.out.println(jsonLD);

            // Create in memory model base in our document in JSON LD format
            Model model = JenaUtil.createMemoryModel();
            InputStream inputStream = new ByteArrayInputStream(jsonLD.getBytes(Charset.defaultCharset()));
            model.read(inputStream, document.location(), "JSON-LD");

            System.out.println("********************");

            // Iterate through triples (Subject - Predicate - Object)
            StmtIterator it = model.listStatements();
            while (it.hasNext()) {
                Statement statement = it.next();

                System.out.println(statement);
            }

            // Make a SPARQL query
            String queryAsString = IOUtils.toString(sparqlInputStream, Charset.defaultCharset());
            Query query = QueryFactory.create(queryAsString);

            System.out.println("********************");
            try (QueryExecution execution = QueryExecutionFactory.create(query, model)) {
                ResultSet rs = execution.execSelect();

                while (rs.hasNext()) {
                    QuerySolution querySolution = rs.next();

                    System.out.println(querySolution);
                }

                System.out.println("Total: " + rs.getRowNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
