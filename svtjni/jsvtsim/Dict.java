package jsvtsim;

public class Dict {
  private int size;
  private dict_t _self;
  public Dict(int size) {
    this.size = size;
    _self = DictImpl.svtsim_dict_new(size);
  }
  public void free() {
    DictImpl.svtsim_dict_free(_self);
  }
  public dict_t cloneIt() {
    return DictImpl.svtsim_dict_clone(_self);
  }
  public int hashIt(final String key) {
    return DictImpl.svtsim_dict_hash(_self, key);
  }
  public int add(final String key, final String value) {
    return DictImpl.svtsim_dict_add(_self, key, value);
  }
  public String query(final String key) {
    return DictImpl.svtsim_dict_query(_self, key);
  }
  public int addOptionString(final String str) {
    return DictImpl.svtsim_dict_addOptionString(_self, str);
  }
  public int addFile(final String fnam) {
    return DictImpl.svtsim_dict_addFile(_self, fnam);
  }
  public int addBlob(final String data, int dlen) {
    return DictImpl.svtsim_dict_addBlob(_self, data, dlen);
  }
  public void dump() {
    DictImpl.svtsim_dict_dump(_self);
  }
  public long crc(final String skipKey) {
    return DictImpl.svtsim_dict_crc(_self, skipKey);
  }
}
