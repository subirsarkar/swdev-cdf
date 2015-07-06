package jsvtsim;

public class WedgeMaps {
  private wedgemaps_t _self;
  public WedgeMaps() {
    this._self = WedgeMapsImpl.wedgeMaps_new();
  }
  protected void finalize() {
    del();
  }
  protected void del() {
    WedgeMapsImpl.wedgeMaps_free(_self);
  }
  public wedgemaps_t getHandle() {
    return _self;
  }
  public int writeMaps() {
    //return WedgeMapsImpl.svtsim_writeRams(_self);
    return 0;
  }
  public int initFromHL(int wedge, final String ifitFnam,
                                   final String pattFnam,
                                   final String ssFnam)
  {
    return WedgeMapsImpl.svtsim_initMaps(_self, wedge, ifitFnam, pattFnam, ssFnam);
  }
  public int initFromMapSet(final int wedge, final String mapSetName)  {
    return initFromMapSet(wedge, mapSetName, 0, "", false);
  }
  public int initFromMapSet(int wedge, 
                            final String mapSetName,
                            long mapSetCrc, 
                            final String dlPath, 
                            boolean useDB) 
  {
    String tstr;
    if (dlPath.equals(""))
       tstr = "/cdf/code-common/cdfonline/svt_config/";
    else
       tstr = dlPath;
    return WedgeMapsImpl.svtsim_initFromMapSet(_self, wedge, mapSetName, mapSetCrc,
                                 tstr, useDB);
  }
  public int useHwSet(int wedge, 
                      final String hwSetName, 
                      long hwSetCrc, 
                      final String dlPath, 
                      boolean useDB) 
  {
    String tstr;
    if (dlPath.equals(""))
       tstr = "/cdf/code-common/cdfonline/svt_config/";
    else
       tstr = dlPath;

    return WedgeMapsImpl.svtsim_useHwSet(_self, wedge, hwSetName, hwSetCrc, tstr, useDB);
  }
  public int patt(int road, int layer) {
    return WedgeMapsImpl.maps_pattSS(_self, road, layer);
  }
  public int [] patt(int road) {
    int [] x = new int[SVTSIM_NLAY];
    for (int i = 0; i < x.length; i++)
      x[i] = patt(road, i);
    return x;
  }
  public int dSSdz(int layer) {
    return WedgeMapsImpl.maps_dSSdz(_self, layer);
  }
  public int ssStrips(int layer) {
    return WedgeMapsImpl.maps_ssStrips(_self, layer);
  }
  public int ssEdge(int layer, int ss) {
    return WedgeMapsImpl.svtsim_ssEdge(_self, layer, ss);
  }
  public int wedge() {
    return WedgeMapsImpl.maps_wedge(_self);
  }
}
