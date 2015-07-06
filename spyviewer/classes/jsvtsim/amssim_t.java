package jsvtsim;

public class amssim_t {
  private long self = System.identityHashCode(amssim_t.class);

  public static Object initializeFromPointer(long p) {
    return new amssim_t(new Long(p));
  }

  public long self() {
    return self;
  }

  public amssim_t(java.lang.Long obj) {
    self = obj.longValue();
  }

  public amssim_t() {}
}
