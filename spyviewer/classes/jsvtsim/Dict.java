package jsvtsim;

public class Dict {
  private int size;
  private dict_t self;
  public Dict(int size) {
    this.size = size;
    self = DictImpl.svtsim_dict_new(size);
  }
  public void free() {
    DictImpl.svtsim_dict_free(self);
  }
  public dict_t cloneIt() {
    return DictImpl.svtsim_dict_clone(self);
  }
  public int hashIt(final String key) {
    return DictImpl.svtsim_dict_hash(self, key);
  }
  public int add(final String key, final String value) {
    return DictImpl.svtsim_dict_add(self, key, value);
  }
  public String query(final String key) {
    return DictImpl.svtsim_dict_query(self, key);
  }
  public int addOptionString(final String str) {
    return DictImpl.svtsim_dict_addOptionString(self, str);
  }
  public int addFile(final String fnam) {
    return DictImpl.svtsim_dict_addFile(self, fnam);
  }
  public int addBlob(final String data, int dlen) {
    return DictImpl.svtsim_dict_addBlob(self, data, dlen);
  }
  public void dump() {
    DictImpl.svtsim_dict_dump(self);
  }
  public long crc(final String skipKey) {
    return DictImpl.svtsim_dict_crc(self, skipKey);
  }
}