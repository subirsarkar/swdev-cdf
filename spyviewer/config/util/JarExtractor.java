package config.util;

import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

public class JarExtractor implements ArchiveExtractor {
  private HashMap<String, String> map = new HashMap<String, String>();
  public JarExtractor (final String infile) {
    JarFile fileObj = null;
    try {
      // JarFile extends ZipFile and adds manifest information
      fileObj = new JarFile(infile);

      // We'll just print out the Manifest attributes
      if (fileObj.getManifest() != null) {
        System.out.println("Manifest Main Attributes:");
        Iterator iter = fileObj.getManifest().getMainAttributes().keySet().iterator();
        while (iter.hasNext()) {
          Attributes.Name attribute = (Attributes.Name) iter.next();
          System.out.println(attribute + " : " +
            fileObj.getManifest().getMainAttributes().getValue(attribute));
        }
        System.out.println();
      }
      // Now the contents of the files
      for (Enumeration e = fileObj.entries(); e.hasMoreElements(); ) {
	JarEntry entry = (JarEntry) e.nextElement();
	final String name = entry.getName();

	if (entry.isDirectory()) {
	  System.err.println("Directory Structure not suppoted: Name: " + name);
	  continue;
        }

	InputStream in = fileObj.getInputStream(entry);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final String buffer = Tools.copyInputStream(in, out);
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
    ArchiveExtractor ae = new JarExtractor(argv[0]);
    for (Map.Entry<String, String> entry : ae.getEntries()) {  
      String key = entry.getKey(); 
      String value = entry.getValue(); 
      System.out.println("============== " + key + "==================");
      System.out.println(value);
    } 
  }
}
