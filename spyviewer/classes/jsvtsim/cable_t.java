package jsvtsim;

public class cable_t {
  private long self = System.identityHashCode(cable_t.class);

  public static Object initializeFromPointer(long p) {
    return new cable_t(new Long(p));
  }

  public long self() {
    return self;
  }

  public cable_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public cable_t() {}
}
