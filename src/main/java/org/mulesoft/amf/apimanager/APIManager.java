package org.mulesoft.amf.apimanager;

import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.Aml10Parser;
import amf.client.render.AmfGraphRenderer;
import amf.client.render.RenderOptions;
import amf.plugins.features.validation.JenaRdfModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.topbraid.jenax.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class APIManager {

    public static void main(String[] args) {
        try {
            ObjectMapper deploymentParser = new ObjectMapper();
            URL deploymentRequest = ClassLoader.getSystemResource("apimanager/deployment-request.json");

            System.out.println("Parse Deployment");
            JsonNode deployment = deploymentParser.readTree(deploymentRequest);

            System.out.println("Show Configuration");
            JsonNode configurationNode = deployment.at("/spec/target/configuration");
            System.out.println(configurationNode);

            System.out.println("Show Environment");
            JsonNode environmentNode = deployment.at("/spec/target/environment");
            System.out.println(environmentNode);

            System.out.println("Init AMF");
            AMF.init().get();

            System.out.println("Register Dialect");
            URL muleApplicationDialect = ClassLoader.getSystemResource("apimanager/generated/mule_application_dialect.raml");
            AMF.registerDialect(muleApplicationDialect.toExternalForm()).get();

            URL corsDialect = ClassLoader.getSystemResource("apimanager/generated/cors_dialect.raml");
            AMF.registerDialect(corsDialect.toExternalForm()).get();

            System.out.println("Create Parsers");
            Aml10Parser amlYamlParser = new Aml10Parser();
            Aml10Parser amlJsonParser = new Aml10Parser("application/json");

            System.out.println("Parse Vocabulary (RDF Model)");
            URL amcVocabularyResource = ClassLoader.getSystemResource("apimanager/generated/mule_application_vocabulary.raml");
            CompletableFuture<BaseUnit> amcVocabularyFuture = amlYamlParser.parseFileAsync(amcVocabularyResource.toExternalForm());
            BaseUnit amcVocabulary = amcVocabularyFuture.get();
            JenaRdfModel vocabularyRDF = (JenaRdfModel) amcVocabulary.toNativeRdfModel(new RenderOptions());

            System.out.println("Parse document");
            ((ObjectNode) configurationNode).put("$dialect", "MuleApplication 0.1");

            System.out.println("Resolve Dynamic Nodes");
            JsonNode policyNode = configurationNode.at("/fragments/policies/0");
            ((ObjectNode) policyNode.get("configuration")).put("$dialect", policyNode.get("type").asText());

            System.out.println(configurationNode);

            CompletableFuture<BaseUnit> future = amlJsonParser.parseStringAsync(configurationNode.toString());
            BaseUnit instance = future.get();

            System.out.println("Instance (RDF Model)");
            JenaRdfModel instanceRDF = (JenaRdfModel) instance.toNativeRdfModel(new RenderOptions());

            System.out.println("Inference Model (JSON-LD)");
            InfModel inferenceModel = ModelFactory.createRDFSModel(vocabularyRDF.model(), instanceRDF.model());
            RDFDataMgr.write(System.out, inferenceModel, RDFFormat.JSONLD);

            System.out.println("Configuration JSON-LD");
            CompletableFuture<String> futureConfigurationJsonLD = new AmfGraphRenderer().generateString(instance);
            String jsonLD = futureConfigurationJsonLD.get();
            System.out.println(jsonLD);

            System.out.println("Jena Memory Model");
            Model inMemoryModel = JenaUtil.createMemoryModel();
            InputStream inputStream = new ByteArrayInputStream(jsonLD.getBytes(Charset.defaultCharset()));
            inMemoryModel.read(inputStream, instance.location(), "JSON-LD");

//            System.out.println("Resolve Dynamic Nodes");
//            InputStream dynamicQueryIS = ClassLoader.getSystemResourceAsStream("apimanager/dynamic.sparql");
//            String dynamicQuery = IOUtils.toString(dynamicQueryIS, Charset.defaultCharset());
//            Query jenaDynamicQuery = QueryFactory.create(dynamicQuery);
//
//            try (QueryExecution execution = QueryExecutionFactory.create(jenaDynamicQuery, inferenceModel)) {
//                ResultSet rs = execution.execSelect();
//
//                String results = ResultSetFormatter.asText(rs);
//                System.out.println(results);
//            }

            System.out.println("Query Assets");
            InputStream assetsQueryIS = ClassLoader.getSystemResourceAsStream("apimanager/assets.sparql");
            String assetsQuery = IOUtils.toString(assetsQueryIS, Charset.defaultCharset());
            Query jenaAssetsQuery = QueryFactory.create(assetsQuery);

            try (QueryExecution execution = QueryExecutionFactory.create(jenaAssetsQuery, inferenceModel)) {
                ResultSet rs = execution.execSelect();

                String results = ResultSetFormatter.asText(rs);
                System.out.println(results);
            }

            System.out.println("Parsing Properties *** MISSING ***");

            System.out.println("Query Secrets");
            InputStream secretsQueryIS = ClassLoader.getSystemResourceAsStream("apimanager/secrets.sparql");
            String secretsQuery = IOUtils.toString(secretsQueryIS, Charset.defaultCharset());
            Query jenaSecretsQuery = QueryFactory.create(secretsQuery);

            try (QueryExecution execution = QueryExecutionFactory.create(jenaSecretsQuery, inferenceModel)) {
                ResultSet rs = execution.execSelect();

                String results = ResultSetFormatter.asText(rs);
                System.out.println(results);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}