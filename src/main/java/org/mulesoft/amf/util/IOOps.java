package org.mulesoft.amf.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Basic I/O util operations.
 */
public interface IOOps {

    default String readUTF8(String resource) {
        final InputStream stream = ClassLoader.getSystemResourceAsStream(resource);
        if(stream == null) throw new IllegalArgumentException("Resource not found: " + resource);
        try {
            return IOUtils.toString(stream, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default URL toURL(String resource) {
        final URL url = ClassLoader.getSystemResource(resource);
        if(url == null) throw new IllegalArgumentException("Resource not found: " + resource);
        return url;
    }

}
