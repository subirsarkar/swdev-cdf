package config.svt;

public interface SvtObject {
    /** Equivalent to <CODE>toString()</CODE> */
  public String getInfo();
    /** Set position of the object in the collection */
  public void setIndex(int index);
    /** Get position of the object in the collection */
  public int getIndex();
}