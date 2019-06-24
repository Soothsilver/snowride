package cz.hudecekpetr.snowride.fx.systemcolor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StringURLConnection extends URLConnection {
    public StringURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() {

    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(SystemColorService.css.getBytes());
    }
}
