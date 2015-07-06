package config.util;

import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FetchURL {
  private URL requestedURL;
  public FetchURL(String urlName) {
    try {
      requestedURL = new URL(urlName);
    }
    catch (java.net.MalformedURLException ex) {
      ex.printStackTrace();
    }
  }
  public InputStream getInputStream() throws IOException {
    return requestedURL.openStream();
  }
  public String toString() {
    InputStream   in = null;
    OutputStream out = null;
    String buffer = new String();
    try {
          in = requestedURL.openStream();
         out = new ByteArrayOutputStream();
      buffer = Tools.copyInputStream(in, out);
    }
    catch (IOException e) {
      e.printStackTrace();
      try {
        in.close();
        out.close();
      }
      catch (IOException ee) {}
    }
    return buffer;
  }
}
