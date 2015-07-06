package jsvtsim;

public class xtfsim_t {
  private long self = System.identityHashCode(xtfsim_t.class);
  public static Object initializeFromPointer(long p) {
    return new xtfsim_t(new Long(p));
  }
  public long self() {
    return self;
  }
  public xtfsim_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public xtfsim_t() {}
}
