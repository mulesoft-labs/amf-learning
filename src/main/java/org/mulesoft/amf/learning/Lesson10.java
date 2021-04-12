package org.mulesoft.amf.learning;


import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.RamlParser;
import amf.client.render.AmfGraphRenderer;
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
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/*
 * We can define hierarchy in our vocabulary and do queries using inferences in our data
 */
public class Lesson10 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            InputStream assetsInputStream = ClassLoader.getSystemResourceAsStream("queries/assets_hierarchy.sparql");

            URL dialectResource = ClassLoader.getSystemResource("dialect/tokenizer_single_dialect.yaml");
            URL dataResource = ClassLoader.getSystemResource("examples/tokenizer_hierarchy.yaml");

            AMF.registerDialect(dialectResource.toExternalForm()).get();

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(dataResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(document);
            String jsonLD = jsonLDFuture.get();

            Model model = ModelFactory.createDefaultModel();
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

            System.out.println("******************** Inf Model ********************");
            StmtIterator infModelIt = infModel.listStatements();
            while (infModelIt.hasNext()) {
                Statement statement = infModelIt.next();

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
