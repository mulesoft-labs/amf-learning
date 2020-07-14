package org.mulesoft.amf.dynamic;

import amf.Core;
import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.Aml10Parser;
import amf.client.render.AmfGraphRenderer;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.topbraid.jenax.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Dynamic {

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            AMF.init().get();

            System.out.println("Register Dialect");
            URL dialectResource = ClassLoader.getSystemResource("dynamic/mule_application.raml");
            AMF.registerDialect(dialectResource.toExternalForm()).get();

            URL dynamicResource = ClassLoader.getSystemResource("dynamic/dynamic.raml");
            AMF.registerDialect(dynamicResource.toExternalForm()).get();

            System.out.println("Create Parser");
            Aml10Parser parser = new Aml10Parser("application/json");

            System.out.println("Get Document to parse");
            URL data = ClassLoader.getSystemResource("dynamic/data.json");
            JsonNode jsonData = mapper.readTree(data);

            System.out.println("Parse document");
            ((ObjectNode) jsonData).put("$dialect", "MuleApplication 0.1");
            ((ObjectNode) jsonData.get("dynamic")).put("$dialect", "Dynamic 0.1");

            System.out.println(jsonData.toString());

            CompletableFuture<BaseUnit> future = parser.parseStringAsync(jsonData.toString());
            BaseUnit baseUnit = future.get();

            System.out.println("Validation");
            CompletableFuture<ValidationReport> validationReportFuture = Core.validate(future.get(), ProfileName.apply("Mule Application 0.1"), MessageStyle.apply("RAML"));
            ValidationReport validationReport = validationReportFuture.get();

            List<ValidationResult> results = validationReport.results();

            for (ValidationResult result: results) {
                System.out.println(result.message());
            }

            System.out.println("Generate JSON-LD");
            CompletableFuture<String> jsonLDFuture = new AmfGraphRenderer().generateString(baseUnit);
            String jsonLD = jsonLDFuture.get();

            System.out.println(jsonLD);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
