package org.mulesoft.amf.amc;

import org.junit.Test;

import static org.junit.Assert.*;
import java.util.stream.Stream;

public class QueryFactorySuite {

    @Test public void testUpdateSecrets() {
        final String query =
                QueryFactory.updateSecrets(Stream.of(
                        ConfSecret.create("http://conf.anypoint.com/asdf", "http://mulesoft.com/vocabularies/amc#keyStorePassword", "http://vault.anypoint.com/asdf"),
                        ConfSecret.create("http://conf.anypoint.com/qwer", "http://mulesoft.com/vocabularies/amc#password", "http://vault.anypoint.com/qwer")
                        ));

        final String expected =
                "DELETE\n" +
                "{\n" +
                "    ?subject ?property ?plain\n" +
                "}\n" +
                "INSERT\n" +
                "{\n" +
                "    ?subject ?property ?encrypted\n" +
                "}\n" +
                "WHERE\n" +
                "{\n" +
                "    ?subject ?property ?plain .\n" +
                "\n" +
                "    VALUES (?subject ?property ?encrypted) {\n" +
                "        (<http://conf.anypoint.com/asdf> <http://mulesoft.com/vocabularies/amc#keyStorePassword> \"http://vault.anypoint.com/asdf\")\n" +
                "        (<http://conf.anypoint.com/qwer> <http://mulesoft.com/vocabularies/amc#password> \"http://vault.anypoint.com/qwer\")\n" +
                "    }\n" +
                "}";

        assertEquals(expected, query);
    }
}
