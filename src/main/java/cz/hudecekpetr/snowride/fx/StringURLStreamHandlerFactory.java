package cz.hudecekpetr.snowride.fx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class StringURLStreamHandlerFactory implements URLStreamHandlerFactory {

    URLStreamHandler streamHandler = new URLStreamHandler(){
        @Override protected URLConnection openConnection(URL url) throws IOException {
            if (url.toString().toLowerCase().endsWith(".css")) {
                return new StringURLConnection(url);
            }
            throw new FileNotFoundException();
        }
    };

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("snow".equals(protocol)) {
            return streamHandler;
        }
        return null;
    }
}
