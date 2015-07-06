package config.db;

import java.io.IOException;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import oracle.sql.BLOB;
import oracle.jdbc.driver.OracleResultSet;
import cdfdb.CdfDb;
import config.util.Tools;

public class SvtDb {
  private static final boolean DEBUG = false;
   /** Static reference to <CODE>this</CODE> */
  private static SvtDb _instance = null;
   /**
    * The constructor could be made private
    * to prevent others from instantiating this class.
    * But this would also make it impossible to
    * create instances of Singleton subclasses.
    */
  private SvtDb() {
    CdfDb.getConnection();
  }
   /** 
    * Singleton constructor. If the class is already instantiated
    * returns the instance, else creates it. Only one object of SvtCrateMap
    * class manages all the interactions using static methods.
    *
    * @return  An instance of <CODE>SvtDb</CODE> class
    */
  public static synchronized SvtDb getInstance() {
    if (_instance == null) {
      _instance = new SvtDb();
    }
    return _instance;
  }
  protected void finalize() throws Throwable {
    CdfDb.closeConnection();
  }
  public void dropConnection() {
    System.out.println("INFO. Disconnecting from Database ...");
    CdfDb.closeConnection();
  }
  public String getData(final String filename) 
     throws IOException, SQLException
  {
    long start = System.currentTimeMillis();
    BLOB blob = null;
    String query = "SELECT DATA FROM SVT_DATA WHERE NAME='"+filename+"'";
    if (DEBUG) System.out.println("Query: " + query);

    Statement stmt  = CdfDb.createStatement();
    ResultSet rset  = stmt.executeQuery(query);
    if (!rset.next())
      System.out.println("SvtDb: Query for <"+filename+ "> returned null ResultSet!");
    else 
      blob = ((OracleResultSet)rset).getBLOB(1);

    if (blob.length() > Integer.MAX_VALUE) 
      throw new ArithmeticException("SvtDb: Blob size <" + blob.length() + "> too big!");

    int len = (int) blob.length();
    byte [] bytesRead = new byte[len];
    bytesRead = blob.getBytes(1, len);

    rset.close();
    stmt.close();

    String s = new String(bytesRead, 0, bytesRead.length);
    long stop = System.currentTimeMillis();
    if (DEBUG) System.out.println("SVT Blob handling time: " 
                       + (stop-start) + " (ms), " + "size: " + (len/1024) + " (Kb)" );
    return s;    
  }

  public String getConfig(final String whichSet) 
     throws IOException, SQLException
  {
    String content = null;
    String query = "SELECT TYPE FROM SVT_DATA WHERE NAME='"+whichSet+"'";
    if (DEBUG) System.out.println("Query: " + query);

    Statement stmt  = CdfDb.createStatement();
    ResultSet rset  = stmt.executeQuery(query);

    if (!rset.next())
      System.out.println("SvtDb: query for current <"+whichSet+"> returned null ResultSet");
    else
      content = rset.getString(1);

    rset.close();
    stmt.close();

    if (DEBUG) System.out.println("Extracted "+whichSet+" name/crc = "+content);

    return content;    
  }
  /** Time stamp */
  public String getTS(final String type) 
     throws IOException, SQLException
  {
    String content = null;
    String query = "SELECT CREATEDATE FROM SVT_DATA WHERE TYPE='" + type + "'";
    if (DEBUG) System.out.println("Query: " + query);

    Statement stmt  = CdfDb.createStatement();
    ResultSet rset  = stmt.executeQuery(query);

    if (!rset.next()) 
      System.out.println("SvtDb: query for current <" + type + "> returned null ResultSet");
    else 
      content = rset.getString(1);

    rset.close();
    stmt.close();

    if (DEBUG) System.out.println("Extracted " + type + " name/crc = " + content);

    return content;    
  }
  public Vector<String> getConfigFiles(final String whichSet) 
     throws IOException, SQLException
  {
    String query = "SELECT TYPE FROM SVT_DATA";
    if (DEBUG) System.out.println("Query: " + query);

    Statement stmt  = CdfDb.createStatement();
    ResultSet rset  = stmt.executeQuery(query);

    Vector<String> vec = new Vector<String>();
    while (rset.next()) {
      String line = rset.getString(1);
      if (line.indexOf(whichSet) == -1) continue;
      String [] fields = line.split("/");
      if (fields.length != 2) 
        throw new RuntimeException("Does not find both the fields, len = " + fields.length);
      
      vec.addElement(fields[0]);
    }

    rset.close();
    stmt.close();

    return vec;    
  }
  public String getConfigFor(int run, final String like) 
     throws IOException, SQLException
  {
    String query  = "SELECT TYPE FROM SVT_DATA " 
                  + "WHERE CREATEDATE="
                  + "  (SELECT MAX(CREATEDATE) FROM SVT_DATA"
                  + "    WHERE CREATEDATE<=("
                  + "      SELECT RC.FETCHTIME FROM RUNCONFIGURATIONS RC"
                  + "        WHERE RC.RUNNUMBER='" + run + "')"
                  + "    AND NAME LIKE '" + like + "')"
                  + "  AND NAME LIKE '" + like + "'";    

    if (DEBUG) System.out.println("Query: " + query);

    Statement stmt  = CdfDb.createStatement();
    ResultSet rset  = stmt.executeQuery(query);

    String type  = new String();
    while (rset.next()) {
      type = rset.getString(1);
      break;
    }
    if (DEBUG) System.out.println("Type: " + type);

    rset.close();
    stmt.close();

    return type;    
  }
}
