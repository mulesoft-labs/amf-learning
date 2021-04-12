package org.mulesoft.amf.learning;

import amf.client.*;
import amf.client.model.document.*;
import amf.client.parse.*;
import amf.client.render.*;
import amf.client.resolve.Async20Resolver;
import amf.client.resolve.Raml10Resolver;
import amf.client.resolve.Resolver;
import org.apache.commons.io.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Since APIs are parsed to a canonical model, the same SPARQL query can be executed against different API formats.
 */
public class Lesson05_01 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            // List of triples with a parser, a model resolver and an input URL for RAML and AsyncAPI
            List<Object[]> inputCases = Arrays.asList(new Object[][] {
              { new RamlParser(), new Raml10Resolver(), ClassLoader.getSystemResource("api/library.raml").toExternalForm() },
              { new Async20Parser(), new Async20Resolver(), ClassLoader.getSystemResource("api/async.yaml").toExternalForm()  },
              { new Async20Parser(), new Async20Resolver(), ClassLoader.getSystemResource("api/async-with-ref.yaml").toExternalForm()  }
            });

            InputStream sparqlInputStream = ClassLoader.getSystemResourceAsStream("queries/endpoints-with-contenttype.sparql");
            String queryAsString = IOUtils.toString(sparqlInputStream, Charset.defaultCharset());

            // For all input format cases, homogeneously find all endpoints that return JSON
            for (Object[] inputCase : inputCases) {
                Parser parser   = (Parser) inputCase[0];
                Resolver resolver   = (Resolver) inputCase[1];
                String inputUrl = (String) inputCase[2];

                CompletableFuture<BaseUnit> parsedModel = parser.parseFileAsync(inputUrl);

                // apply a transformation over the parsed model that resolves references, suppresses declarations, expands
                // type references/extensions, normalizes parameter types, deletes external references, etc.
                // The relevant piece for the async examples is references resolution
                CompletableFuture<BaseUnit> resolvedModel = parsedModel.thenApply(resolver::resolve);
                Document                    document      = (Document) resolvedModel.get();

                CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(document);
                String                    jsonLD       = jsonLDFuture.get();

                // Create in memory model base in our document in JSON LD format
                Model model = ModelFactory.createDefaultModel();
                InputStream inputStream = new ByteArrayInputStream(jsonLD.getBytes(Charset.defaultCharset()));
                model.read(inputStream, document.location(), "JSON-LD");

                // Make a SPARQL query
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
