package cz.hudecekpetr.snowride.fx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StringURLConnection extends URLConnection {
    public StringURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(SystemColorService.css.getBytes());
    }
}
