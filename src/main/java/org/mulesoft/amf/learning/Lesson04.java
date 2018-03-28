package org.mulesoft.amf.learning;

import amf.Core;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.parse.RamlParser;
import amf.client.validate.ValidationReport;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * AMF allows us to create a custom validation profile to validate our document
 */
public class Lesson04 {
    public static void main(String[] args) {
        try {
            Core.init().get();
            amf.plugins.features.AMFValidation.register();

            URL systemResource = ClassLoader.getSystemResource("api/library.raml");
            URL validationProfileResource = ClassLoader.getSystemResource("validations/validation_profile.raml");

            RamlParser parser = new RamlParser();
            CompletableFuture<BaseUnit> parseFileAsync = parser.parseFileAsync(systemResource.toExternalForm());
            Document document = (Document) parseFileAsync.get();

            String title = "RAML";
            String validationProfile = "RAML";
            CompletableFuture<ValidationReport> validationReportFuture = Core.validate(document, title, validationProfile);

            ValidationReport validationReport = validationReportFuture.get();

            System.out.println(validationReport);

            System.out.println("********************");
            String secondTitle = "RAML";
            String secondValidationProfile = validationProfileResource.toExternalForm();
            CompletableFuture<ValidationReport> secondValidationReportFuture = Core.validate(document, secondTitle, secondValidationProfile);

            ValidationReport secondValidationReport = secondValidationReportFuture.get();

            System.out.println(secondValidationReport);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
