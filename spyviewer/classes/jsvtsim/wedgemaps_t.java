package jsvtsim;

public class wedgemaps_t {
  private long self = System.identityHashCode(wedgemaps_t.class);

  public static Object initializeFromPointer(long p) {
    return new wedgemaps_t(new Long(p));
  }

  public long self() {
    return self;
  }

  public wedgemaps_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public wedgemaps_t() {}
}
