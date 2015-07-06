package config.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class FileTypeFilter extends FileFilter {
  public final static String GZ = "gz";
  public final static String TGZ = "tar.gz";
  public final static String TAR = "tar";
  public final static String ZIP = "zip";
  public final static String JAR = "jar";
  public final static String DAT = "dat";
  public final static String XML = "xml";
  public final static String HTTP = "http://";

  // Accept all directories and all tar.gz, gz, zip, tar, dat, xml
  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }
    String extension = getExtension(f);
    if (extension != null) {
      if (extension.equals(DAT) ||
          extension.equals(XML) ||
          extension.equals(GZ)  ||
          isTgz(f)              ||
          extension.equals(TAR) ||
          extension.equals(ZIP) ||
          extension.equals(JAR) ) return true;
      else
        return false;
    }

    return false;
  }

  // The description of this filter
  public String getDescription() {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    buf.append(DAT).append(",");
    buf.append(XML).append(",");
    buf.append(GZ).append(",");
    buf.append(TGZ).append(",");
    buf.append(TAR).append(",");
    buf.append(ZIP).append(",");
    buf.append(JAR);

    return buf.toString();
  }
  public static boolean isTgz(final File f) {
    return isTgz(f.getName());
  }
  public static boolean isTgz(final String s) {
    return s.endsWith(TGZ);
  }
  public static boolean isTar(final File f) {
    return isTar(f.getName());
  }
  public static boolean isTar(final String s) {
    String ext = getExtension(s);
    return ext.equals(TAR);
  }
  public static boolean isXML(final File f) {
    return isXML(f.getName());
  }
  public static boolean isXML(final String s) {
    String ext = getExtension(s);
    return ext.equals(XML);
  }
  public static boolean isPlain(final File f) {
    return isPlain(f.getName());
  }
  public static boolean isPlain(final String s) {
    String ext = getExtension(s);
    return ext.equals(DAT);
  }
  public static boolean isGzipped(final File f) {
    return isGzipped(f.getName());
  }
  public static boolean isGzipped(final String s) {
    String ext = getExtension(s);
    return ext.equals(GZ);
  }
  public static boolean isWebFile(final String s) {
    return s.startsWith(HTTP);
  }
  public static boolean isArchive(File f) {
    return isArchive(f.getName());
  }
  public static boolean isArchive(String s) {
    String ext = getExtension(s);
    if ( s.endsWith(TGZ) ||
         ext.equals(TAR) ||
         ext.equals(ZIP) ||
         ext.equals(JAR) ) return true;
    else 
      return false;
  }
  /*
   * Get the extension of a file.
   */
  public static String getExtension(File f) {
    return getExtension(f.getName());
  }
  public static String getExtension(final String s) {
    String ext = null;
    int i = s.lastIndexOf('.');

    if (i > 0 &&  i < s.length() - 1)
      ext = s.substring(i+1).toLowerCase();
    return ext;
  }
}
