package config.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

public class Gunzip {
  String buffer = null;
  public Gunzip(final String filename) {
    InputStream   in = null;
    OutputStream out = null;
    try {
          in = new GZIPInputStream(new FileInputStream(filename));
         out = new ByteArrayOutputStream();
      buffer = Tools.copyInputStream(in, out);
    } 
    catch (java.io.FileNotFoundException fnfe) {
      System.err.println("File was not found: " + fnfe.getMessage());
    } 
    catch (java.io.IOException e) {
      System.err.println("An IOException occurred: " + e.getMessage());
      try {
        in.close();
        out.close();
      }
      catch (java.io.IOException ee) {}
    } 
  }
  public Gunzip(final InputStream istream) {
    InputStream   in = null;
    OutputStream out = null;
    try {
          in = new GZIPInputStream(istream);
         out = new ByteArrayOutputStream();
      buffer = Tools.copyInputStream(in, out);
    } 
    catch (java.io.FileNotFoundException fnfe) {
      System.err.println("File was not found: " + fnfe.getMessage());
    } 
    catch (java.io.IOException e) {
      System.err.println("An IOException occurred: " + e.getMessage());
      try {
        in.close();
        out.close();
      }
      catch (java.io.IOException ee) {}
    } 
  }
  public String getContent() {
    return buffer;
  }
}
