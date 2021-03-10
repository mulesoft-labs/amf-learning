package org.mulesoft.amf.amc;

import java.util.stream.Stream;

public class QueryFactory {

    public static String updateSecrets(Stream<ConfSecret> secrets) {
        final StringBuilder builder =
                new StringBuilder()
                        .append("DELETE\n").append("{\n")
                        .append("    ?subject ?property ?plain\n")
                        .append("}\n")
                        .append("INSERT\n").append("{\n")
                        .append("    ?subject ?property ?encrypted\n")
                        .append("}\n")
                        .append("WHERE\n").append("{\n")
                        .append("    ?subject ?property ?plain .\n").append("\n")
                        .append("    VALUES (?subject ?property ?encrypted) {\n");
        secrets.forEach(s -> builder.append("        (<").append(s.getSubject()).append("> <").append(s.getProperty()).append("> \"").append(s.getValue()).append("\")\n"));
        builder.append("    }\n").append("}");
        return builder.toString();
    }
}
