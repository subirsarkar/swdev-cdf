package config.util;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarEntry;

public class TarExtractor implements ArchiveExtractor {
  private HashMap<String, String> map = new HashMap<String,String>();
  public TarExtractor (final String infile) {
    String ext = FileTypeFilter.getExtension(infile);
    TarInputStream tin = null; 
    try {
      if ( FileTypeFilter.isWebFile(infile) ) {
	InputStream stream = new FetchURL(infile).getInputStream();
        if (FileTypeFilter.isTgz(infile))
          tin = new TarInputStream(new GZIPInputStream(stream));
        else 
          tin = new TarInputStream(stream);
      }
      else {
        if (FileTypeFilter.isTgz(infile))
          tin = new TarInputStream(new GZIPInputStream(new FileInputStream(new File(infile))));
        else 
          tin = new TarInputStream(new FileInputStream(new File(infile)));
      } 
      TarEntry entry; 
      // Get the entries in the archive                          
      while ((entry = tin.getNextEntry()) != null) {  
        String name = entry.getName();
        if (entry.isDirectory()) {
          System.err.println("Directory Structure not suppoted: Name: " + name);
          continue;
        }
	else {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          tin.copyEntryContents(out);   
          map.put(name, out.toString());
          out.close();                      
        }
      }    
      tin.close();
    } 
    catch (FileNotFoundException fnfe) {
      System.out.println("An IOException occurred: " + fnfe.getMessage());
    } 
    catch (IOException ioe) {
      System.out.println("An IOException occurred: " + ioe.getMessage());
    } 
    finally {
      if (tin != null) {
        try {
          tin.close(); 
        } catch (IOException ioe) {}
      }
    }
  }
  public Set<Map.Entry<String, String>> getEntries() {
    return map.entrySet();
  }
  public static void main(String [] argv) {
    ArchiveExtractor ae = new TarExtractor(argv[0]);
    for (Map.Entry<String, String> entry : ae.getEntries()) {  
      String key = entry.getKey(); 
      String value = entry.getValue(); 
      System.out.println("============== " + key + "==================");
      System.out.println(value);
    } 
  }
}
