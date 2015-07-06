package jsvtsim;

public class mrgsim_t {
  private long self = System.identityHashCode(mrgsim_t.class);
  public static Object initializeFromPointer(long p) {
    return new mrgsim_t(new Long(p));
  }
  public long self() {
    return self;
  }
  public mrgsim_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public mrgsim_t() {}
}
