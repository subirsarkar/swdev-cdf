package jsvtsim;

public class WedgeMaps implements SvtsimConstants {
  private wedgemaps_t maps;
  public WedgeMaps() {
    this.maps = WedgeMapsImpl.wedgeMaps_new();
  }
  protected void finalize() {
    del();
  }
  protected void del() {
    WedgeMapsImpl.wedgeMaps_free(maps);
  }
  public wedgemaps_t getHandle() {
    return maps;
  }
  public int writeMaps() {
    //return WedgeMapsImpl.svtsim_writeRams(maps);
    return 0;
  }
  public int initFromHL(final int wedge, final String ifitFnam,
                                            final String pattFnam,
                                            final String ssFnam)
  {
    return WedgeMapsImpl.svtsim_initMaps(maps, wedge, ifitFnam, pattFnam, ssFnam);
  }
  public int initFromMapSet(final int wedge, final String mapSetName)
  {
    return initFromMapSet(wedge, mapSetName, 0, "", false);
  }
  public int initFromMapSet(final int wedge, final String mapSetName,
                            final long mapSetCrc, final String dlPath, 
                            final boolean useDB) 
  {
    String tstr;
    if (dlPath.equals(""))
       tstr = "/cdf/code-common/cdfonline/svt_config/";
    else
       tstr = dlPath;
    return WedgeMapsImpl.svtsim_initFromMapSet(maps, wedge, mapSetName, mapSetCrc,
                                 tstr, useDB);
  }
  public int useHwSet(final int wedge, final String hwSetName, 
                      final long hwSetCrc, final String dlPath, 
                      final boolean useDB) 
  {
    String tstr;
    if (dlPath.equals(""))
       tstr = "/cdf/code-common/cdfonline/svt_config/";
    else
       tstr = dlPath;

    return WedgeMapsImpl.svtsim_useHwSet(maps, wedge, hwSetName, hwSetCrc, tstr, useDB);
  }
  public int patt(final int road, final int layer) {
    return WedgeMapsImpl.maps_pattSS(maps, road, layer);
  }
  public int [] patt(final int road) {
    int [] x = new int[SVTSIM_NLAY];
    for (int i = 0; i < x.length; i++)
      x[i] = patt(road, i);
    return x;
  }
  public int dSSdz(final int layer) {
    return WedgeMapsImpl.maps_dSSdz(maps, layer);
  }
  public int ssStrips(final int layer) {
    return WedgeMapsImpl.maps_ssStrips(maps, layer);
  }
  public int ssEdge(final int layer, final int ss) {
    return WedgeMapsImpl.svtsim_ssEdge(maps, layer, ss);
  }
  public int wedge() {
    return WedgeMapsImpl.maps_wedge(maps);
  }
}