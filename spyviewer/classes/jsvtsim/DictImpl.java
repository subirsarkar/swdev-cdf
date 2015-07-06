package jsvtsim;

/**
  *   DictImpl.java
  *
  *   <P>
  *   Implements Bills dictionary
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class DictImpl {
  public static native dict_t svtsim_dict_new(int hashsize);
  public static native dict_t svtsim_dict_clone(final dict_t self);
  public static native void svtsim_dict_free(final dict_t self);
  public static native int svtsim_dict_hash(final dict_t self, final String key);
  public static native int svtsim_dict_add(final dict_t self, final String key, final String value);
  public static native String svtsim_dict_query(final dict_t self, final String key);
  public static native int svtsim_dict_addOptionString(final dict_t self, final String str);
  public static native int svtsim_dict_addFile(final dict_t self, final String fnam);
  public static native int svtsim_dict_addBlob(final dict_t self, final String data, int dlen);
  public static native void svtsim_dict_dump(final dict_t self);
  public static native long svtsim_dict_crc(final dict_t self, final String skipKey);
}
