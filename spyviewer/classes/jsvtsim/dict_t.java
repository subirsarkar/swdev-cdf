package jsvtsim;

public class dict_t {
  private long self = System.identityHashCode(dict_t.class);

  public static Object initializeFromPointer(long p) {
    return new dict_t(new Long(p));
  }

  public long self() {
    return self;
  }

  public dict_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public dict_t() {}
}
