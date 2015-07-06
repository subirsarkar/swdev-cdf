package jsvtsim;

public class hfsim_t {
  private long self = System.identityHashCode(hfsim_t.class);
  public static Object initializeFromPointer(long p) {
    return new hfsim_t(new Long(p));
  }
  public long self() {
    return self;
  }
  public hfsim_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public hfsim_t() {}
}
