package org.mulesoft.amf.learning;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Lesson15 {

    public static void main(String[] args) {
        try {
            JSON2YAML("ejemplo1.json");
            JSON2YAML("ejemplo2.json");
            JSON2YAML("ejemplo3.json");
            JSON2YAML("ejemplo4.json");
            JSON2YAML("ejemplo5.json");

            System.out.println("--------------------");

            YAML2JSON("ejemplo1.yaml");
            YAML2JSON("ejemplo2.yaml");
            YAML2JSON("ejemplo3.yaml");
            YAML2JSON("ejemplo4.yaml");
            YAML2JSON("ejemplo5.yaml");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void JSON2YAML(String fileName) throws IOException {
        String json = IOUtils.toString(ClassLoader.getSystemResourceAsStream(fileName), StandardCharsets.UTF_8);

        System.out.println("*******************");
        System.out.println("JSON");
        System.out.println("*******************");
        System.out.println(json);

        System.out.println("*******************");
        System.out.println("YAML");
        System.out.println("*******************");
        System.out.println(asYaml(json));
    }

    private static void YAML2JSON(String fileName) throws IOException {
        String yaml = IOUtils.toString(ClassLoader.getSystemResourceAsStream(fileName), StandardCharsets.UTF_8);

        System.out.println("*******************");
        System.out.println("YAML");
        System.out.println("*******************");
        System.out.println(yaml);

        System.out.println("*******************");
        System.out.println("JSON");
        System.out.println("*******************");
        System.out.println(asJson(yaml));
    }

    private static String asJson(String jsonString) throws JsonProcessingException, IOException {
        // Parse YAML
        YAMLMapper yamlMapper = new YAMLMapper();
        JsonNode yamlNodeTree = yamlMapper.readTree(jsonString);

        // Save it as JSON
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(yamlNodeTree);
    }

    private static String asYaml(String jsonString) throws JsonProcessingException, IOException {
        // Parse JSON
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode jsonNodeTree = jsonMapper.readTree(jsonString);

        // Save it as YAML
        YAMLMapper yamlMapper = new YAMLMapper();
        return yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNodeTree);
    }
}
