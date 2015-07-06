package jsvtsim;

public class tfsim_t {
  private long self = System.identityHashCode(tfsim_t.class);
  public static Object initializeFromPointer(long p) {
    return new tfsim_t(new Long(p));
  }
  public long self() {
    return self;
  }
  public tfsim_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public tfsim_t() {}
}
