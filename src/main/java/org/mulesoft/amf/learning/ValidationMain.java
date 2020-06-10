package org.mulesoft.amf.learning;

import amf.Core;
import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.model.document.BaseUnit;
import amf.client.parse.Aml10Parser;
import amf.client.validate.ValidationReport;
import amf.plugins.document.Vocabularies;
import com.google.common.base.Stopwatch;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * AMF allows us to create a custom validation profile to validate our document
 */
public class ValidationMain {

    private static final String DIALECTS_PATH = System.getProperty("user.dir") + "/dialect/mule_application.dialect";
    private static final String INPUT = System.getProperty("user.dir") + "/input/mule_application.json";

    public static void main(String[] args) {
        try {
            AMF.init().get();

            File file = new File(DIALECTS_PATH);
            Vocabularies.registerDialect(file.toURI().toURL().toExternalForm()).get();

            Aml10Parser parser = new Aml10Parser(APPLICATION_JSON.toString());
            String document = new String(Files.readAllBytes( Paths.get(INPUT)));
            String dialect = "MuleApplication 0.1";
            BaseUnit baseUnit = parser.parseStringAsync(document).get();

            ValidationReport report = Core.validate(baseUnit, ProfileName.apply(dialect), MessageStyle.apply("JSON")).get();
            System.out.println(report.conforms());
            if(!report.conforms()) {
                report.results().forEach(result -> System.out.println(result.message()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
