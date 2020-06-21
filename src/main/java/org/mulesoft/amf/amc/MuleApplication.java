package org.mulesoft.amf.amc;

import amf.Core;
import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.Aml10Parser;
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
import org.topbraid.jenax.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MuleApplication {

    public static void main(String[] args) {
        try {
            AMF.init().get();

            System.out.println("Register Dialect");
            URL dialectResource = ClassLoader.getSystemResource("dialect/mule_application_dialect.raml");
            AMF.registerDialect(dialectResource.toExternalForm()).get();

            System.out.println("Create Parser");
            Aml10Parser parser = new Aml10Parser("application/json");

            System.out.println("Get Document to parse");
            URL agentExample = ClassLoader.getSystemResource("examples/agent4.json");

            System.out.println("Parse document");
            CompletableFuture<BaseUnit> future = parser.parseFileAsync(agentExample.toExternalForm());
            BaseUnit baseUnit = future.get();

            System.out.println("Validation");
            CompletableFuture<ValidationReport> validationReportFuture = Core.validate(future.get(), ProfileName.apply("Mule Application 0.1"), MessageStyle.apply("RAML"));
            ValidationReport validationReport = validationReportFuture.get();

            if (!validationReport.conforms()) {
                List<ValidationResult> results = validationReport.results();
                for (ValidationResult result : results) {
                    System.out.println(result.message());
                }
            }

            System.out.println("Generate JSON-LD");
            CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(baseUnit);
            String jsonLD = jsonLDFuture.get();

            System.out.println(jsonLD);

            System.out.println("Create JENA Model");
            Model model = JenaUtil.createMemoryModel();
            InputStream inputStream = new ByteArrayInputStream(jsonLD.getBytes(Charset.defaultCharset()));
            model.read(inputStream, baseUnit.location(), "JSON-LD");

            System.out.println("SPARQL");
            InputStream queryInputStream = ClassLoader.getSystemResourceAsStream("queries/secrets.sparql");
            String queryString = IOUtils.toString(queryInputStream, Charset.defaultCharset());
            Query query = QueryFactory.create(queryString);

            try (QueryExecution execution = QueryExecutionFactory.create(query, model)) {
                ResultSet rs = execution.execSelect();

                while (rs.hasNext()) {
                    QuerySolution querySolution = rs.next();

                    System.out.println(querySolution);
                }

                System.out.println("Total: " + rs.getRowNumber());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
