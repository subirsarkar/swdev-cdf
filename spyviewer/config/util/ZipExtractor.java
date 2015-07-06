package config.util;

import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class ZipExtractor implements ArchiveExtractor {
  private HashMap<String, String> map = new HashMap<String, String>();
    //  public ZipExtractor (final InputStream stream) {
    //}
  public ZipExtractor (final String infile) {
    ZipFile fileObj = null;
    try {
      fileObj = new ZipFile(infile);

      // ZipFile offers an Enumeration of all the files in the Zip file
      for (Enumeration e = fileObj.entries(); e.hasMoreElements(); ) {
	ZipEntry entry = (ZipEntry) e.nextElement();
	String name = entry.getName();
	if (entry.isDirectory()) {
	  System.err.println("Directory Structure not suppoted: Name: " + name);
	  continue;
        }

	InputStream in = fileObj.getInputStream(entry);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String buffer = Tools.copyInputStream(in, out);
        map.put(name, buffer);
      }
    } 
    catch (FileNotFoundException fnfe) {
      System.out.println("An IOException occurred: " + fnfe.getMessage());
    } 
    catch (IOException ioe) {
      System.out.println("An IOException occurred: " + ioe.getMessage());
    } 
    finally {
      if (fileObj != null) {
        try {
          fileObj.close(); 
        } catch (IOException ioe) {}
      }
    }
  }
  public Set<Map.Entry<String, String>> getEntries() {
    return map.entrySet();
  }
  public static void main(String [] argv) {
    ArchiveExtractor ae = new ZipExtractor(argv[0]);
    for (Map.Entry<String, String> entry : ae.getEntries()) {  
      String key = entry.getKey(); 
      String value = entry.getValue(); 
      System.out.println("============== " + key + "==================");
      System.out.println(value);
    } 
  }
}
