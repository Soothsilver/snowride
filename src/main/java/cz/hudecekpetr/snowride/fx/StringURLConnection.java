package cz.hudecekpetr.snowride.fx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

public class StringURLConnection extends URLConnection {
    @Override
    public void connect() throws IOException {

    }

    public StringURLConnection(URL url) {
        super(url);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream( SystemColorService.css.getBytes() );
    }
}
