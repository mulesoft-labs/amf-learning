package org.mulesoft.amf.learning;


import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.RamlParser;
import amf.client.render.AmfGraphRenderer;
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
import org.topbraid.spin.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
public class Lesson13 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            InputStream assetsInputStream = ClassLoader.getSystemResourceAsStream("queries/complex_assets_hierarchy.sparql");


            URL dataResource = ClassLoader.getSystemResource("examples/tokenizer_hierarchy.raml");

            InputStream dialectInputStream = ClassLoader.getSystemResourceAsStream("dialect/tokenizer_single_dialect.raml");
            Vocabularies.registerDialect("http://deployer.org/dialects/Tokenizer", IOUtils.toString(dialectInputStream, StandardCharsets.UTF_8)).get();

            // URL dialectResource = ClassLoader.getSystemResource("dialect/tokenizer_single_dialect.raml");
            // AMF.registerDialect(dialectResource.toExternalForm()).get();

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(dataResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(document);
            String jsonLD = jsonLDFuture.get();

            Model model = JenaUtil.createMemoryModel();
            InputStream inputStream = new ByteArrayInputStream(jsonLD.getBytes(Charset.defaultCharset()));
            model.read(inputStream, document.location(), "JSON-LD");

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
}
