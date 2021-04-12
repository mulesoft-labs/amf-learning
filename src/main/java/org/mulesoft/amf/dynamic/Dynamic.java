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

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Dynamic {

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            AMF.init().get();

            System.out.println("Register Dialect");
            URL dialectResource = ClassLoader.getSystemResource("dynamic/mule_application.yaml");
            AMF.registerDialect(dialectResource.toExternalForm()).get();

            System.out.println("Create Parser");
            Aml10Parser parser = new Aml10Parser("application/json");

            System.out.println("Get Document to parse");
            URL data = ClassLoader.getSystemResource("dynamic/data.json");
            JsonNode jsonData = mapper.readTree(data);

            System.out.println("Parse document");
            ((ObjectNode) jsonData).put("$dialect", "MuleApplication 0.1");
            ((ObjectNode) jsonData.get("dynamic")).put("$dialect", "file:///Users/ldebello/repos/amf-learning/src/main/resources/dynamic/dynamic.yaml#/declarations/RootNode");

            System.out.println(jsonData.toString());

            CompletableFuture<BaseUnit> future = parser.parseStringAsync(jsonData.toString());
            BaseUnit baseUnit = future.get();

            System.out.println("Validation");
            CompletableFuture<ValidationReport> validationReportFuture = Core.validate(future.get(), ProfileName.apply("Mule Application 0.1"), MessageStyle.apply("RAML"));
            ValidationReport validationReport = validationReportFuture.get();

            List<ValidationResult> validationResults = validationReport.results();

            for (ValidationResult result: validationResults) {
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
