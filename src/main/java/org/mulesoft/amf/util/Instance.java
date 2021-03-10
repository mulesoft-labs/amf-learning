package org.mulesoft.amf.util;

import java.net.URL;

public class Instance implements IOOps {

    private final String uri;
    private final String dialect;
    private final String mediaType;

    private Instance(String uri, String dialect, String mediaType) {
        this.uri = uri;
        this.dialect = dialect;
        this.mediaType = mediaType;
    }

    public String getUri() { return toURL(uri).toExternalForm(); }

    public String getDialect() { return dialect; }

    public String getMediaType() { return mediaType; }

    public static Instance createVocabulary(String uri) { return create(uri, "Vocabulary 1.0"); }

    public static Instance createDialect(String uri) { return create(uri, "Dialect 1.0"); }

    public static Instance create(String uri, String dialect) { return create(uri, dialect, "application/yaml"); }

    public static Instance create(String uri, String dialect, String mediaType) { return new Instance(uri, dialect, mediaType); }
}
