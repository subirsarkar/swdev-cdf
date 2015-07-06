package jsvtvme;

public class svtvme_t {
  private long self = System.identityHashCode(svtvme_t.class);
  public static Object initializeFromPointer(long p) {
    return new svtvme_t(new Long(p));
  }
  public long self() {
    return self;
  }
  public svtvme_t(java.lang.Long obj) {
    self = obj.longValue();
  }
  public svtvme_t() {}
}
