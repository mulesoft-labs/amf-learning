package org.mulesoft.amf.learning;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import amf.Core;
import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.RamlParser;
import amf.client.render.AmfGraphRenderer;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
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

/*
 * We can create a custom dialect to map our input source to our canonical model, also using the dialect we can validate and do
 * conversion back and forth.
 */
public class Lesson06
{

    public static void main(String[] args)
    {
        try
        {
            AMF.init().get();

            URL dialectResource = ClassLoader.getSystemResource("mule-runtime/runtime-dialect.raml");
            URL dataResource = ClassLoader.getSystemResource("mule-runtime/runtime-example1.raml");

            AMF.registerDialect(dialectResource.toExternalForm()).get();

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(dataResource.toExternalForm());
            BaseUnit document = parseFileAsync.get();

            ValidationReport validationReport = Core.validate(document, "MuleRuntime 0.1", "RAML").get();
            boolean conforms = validationReport.conforms();
            if (!conforms)
            {
                for (ValidationResult validationResult : validationReport.results())
                {
                    System.out.println(validationResult.message());
                }
            }

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
            while (it.hasNext())
            {
                Statement statement = it.next();

                System.out.println(statement);
            }

            System.out.println("********************");
            System.out.println(document);


            // Make a SPARQL query
            InputStream assetsInputStream = ClassLoader.getSystemResourceAsStream("mule-runtime/queries/list-flows.sparql");

            String queryAsString = IOUtils.toString(assetsInputStream, Charset.defaultCharset());
            Query query = QueryFactory.create(queryAsString);

            System.out.println("********************");
            StmtIterator statementIterator = model.listStatements();
            while (statementIterator.hasNext())
            {
                Statement statement = statementIterator.next();

                System.out.println(statement);
            }

            System.out.println("********************");
            try (QueryExecution execution = QueryExecutionFactory.create(query, model))
            {
                ResultSet rs = execution.execSelect();

                while (rs.hasNext())
                {
                    QuerySolution querySolution = rs.next();

                    System.out.println(querySolution);
                }

                System.out.println("Total: " + rs.getRowNumber());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
