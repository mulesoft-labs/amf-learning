package org.mulesoft.amf.learning;

import amf.Core;
import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.model.document.BaseUnit;
import amf.client.parse.Aml10Parser;
import amf.plugins.document.Vocabularies;
import com.google.common.base.Stopwatch;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * AMF allows us to create a custom validation profile to validate our document
 */
public class ValidationMain {

    private static final String DIALECTS_PATH = System.getProperty("user.dir") + "/dialect/mule_application.dialect";

    public static void main(String[] args) {
        try {
            AMF.init().get();

            ClassLoader classLoader = ValidationMain.class.getClassLoader();
            File file = new File(DIALECTS_PATH);
            Vocabularies.registerDialect(file.toURI().toURL().toExternalForm()).get();

            Aml10Parser parser = new Aml10Parser(APPLICATION_JSON.toString());
            String document = "{\"$dialect\":\"MuleApplication 0.1\"}";
            String dialect = "MuleApplication 0.1";
            BaseUnit baseUnit = parser.parseStringAsync(document).get();

            Stopwatch sw = Stopwatch.createStarted();
            System.out.println("Starting 1st parse");
            long start = 0;
            long finish;

            for (int i = 0; i < 10; i++) {
                Core.validate(baseUnit, ProfileName.apply(dialect), MessageStyle.apply("RAML")).get();

                finish = sw.elapsed(TimeUnit.MILLISECONDS);
                System.out.println("Validate took " + (finish - start) + "ms.");
                start = finish;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
