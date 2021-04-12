package org.mulesoft.amf.learning;

import amf.Core;
import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.parse.RamlParser;
import amf.client.validate.ValidationReport;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * AMF allows us to create a custom validation profile to validate our document
 *
 * NOT WORKING FIX THIS!!!
 */
public class Lesson04 {
    public static void main(String[] args) {
        try {
            AMF.init().get();

            URL systemResource = ClassLoader.getSystemResource("api/library.raml");

            URL validationProfileResource = ClassLoader.getSystemResource("validations/validation_profile.yaml");

            RamlParser parser = new RamlParser();

            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());

            BaseUnit document = parseFileAsync.get();

            CompletableFuture<ValidationReport> future = Core.validate(document, ProfileName.apply("Validation Profile 1.0"), MessageStyle.apply("JSON"));


            ValidationReport report = future.get();

            System.out.println(report.conforms());
            if (!report.conforms()) {
                report.results().forEach(result -> System.out.println(result.message()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
