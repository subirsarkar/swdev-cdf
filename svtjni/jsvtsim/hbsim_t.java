package jsvtsim;

public class hbsim_t {
  private long self = System.identityHashCode(hbsim_t.class);
  public static Object initializeFromPointer(long p) {
    return new hbsim_t(new Long(p));
  }
  public long self() {
    return self;
  }
  public hbsim_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public hbsim_t() {}
}
