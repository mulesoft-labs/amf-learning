package org.mulesoft.amf.learning;


import amf.client.AMF;
import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.model.document.BaseUnit;
import amf.client.parse.Aml10Parser;
import amf.client.remote.Content;
import amf.client.render.AmfGraphRenderer;
import amf.client.resource.ResourceLoader;
import amf.plugins.document.Vocabularies;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/*
 * SPARQL provides different features to query triples
 *
 * # prefix declarations
 * PREFIX foo: <http://example.com/resources/>
 * ...
 * # dataset definition
 * FROM ...
 * # result clause
 * SELECT ...
 * # query pattern
 * WHERE {
 *  ...
 *  }
 *  # query modifiers
 *  ORDER BY ...
 *
 * The SPARQL keyword a is a shortcut for the common predicate rdf:type, giving the class of a resource.
 * Shortcut: a semicolon (;) can be used to separate two triple patterns that share the same subject. (?country is the shared subject above.)
 */
public class Lesson14 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            InputStream assetsInputStream = ClassLoader.getSystemResourceAsStream("queries/complex_assets_hierarchy.sparql");

            URL dialectResource = ClassLoader.getSystemResource("dialect/tokenizer_single_dialect.yaml");
            URL dataResource = ClassLoader.getSystemResource("examples/tokenizer_hierarchy.yaml");

            Environment env = DefaultEnvironment.apply().add(new JarResourceLoader());

            Vocabularies.registerDialect(dialectResource.toExternalForm(), env).get();

            Aml10Parser parser = new Aml10Parser(env);
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(dataResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(document);
            String jsonLD = jsonLDFuture.get();

            Model model = ModelFactory.createDefaultModel();
            InputStream inputStream = new ByteArrayInputStream(jsonLD.getBytes(Charset.defaultCharset()));
            String location = document.location();
            model.read(inputStream, location, "JSON-LD");

            Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(model);
            InfModel infModel = ModelFactory.createInfModel(reasoner, model);

            System.out.println("******************** Model ********************");
            StmtIterator modelIt = model.listStatements();
            while (modelIt.hasNext()) {
                Statement statement = modelIt.next();

                System.out.println(statement);
            }

            System.out.println("********************");

            // Make a SPARQL query
            String queryAsString = IOUtils.toString(assetsInputStream, Charset.defaultCharset());
            Query query = QueryFactory.create(queryAsString);

            try (QueryExecution execution = QueryExecutionFactory.create(query, infModel)) {
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

    private static class JarResourceLoader implements ResourceLoader {

        @Override
        public CompletableFuture<Content> fetch(String resource) {
            try {
                System.out.println("Fetching: " + resource);
                URL jarUrl = new URL(resource);
                JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
                InputStream resourceInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(connection.getEntryName());
                Content content = new Content(IOUtils.toString(resourceInputStream, StandardCharsets.UTF_8), resource);
                return CompletableFuture.supplyAsync(() -> content);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return fail();
        }

        @Override
        public boolean accepts(String resource) {
            System.out.println("Accepts: " + resource);
            boolean accepts = false;
            try {

                URL url = new URL(resource);
                accepts = "jar".equals(url.getProtocol());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return accepts;
        }

        private CompletableFuture<Content> fail() {
            return CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Failed to apply.");
            });
        }
    }
}
