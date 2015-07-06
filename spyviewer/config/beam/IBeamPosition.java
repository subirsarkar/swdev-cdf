package config.beam;

import com.smartsockets.TipcMsg;

public interface IBeamPosition {
  public void update(TipcMsg msg);
  public String getData();
  public double [] getXDataArray();
  public double [] getYDataArray();
  public double [] getXErrorArray();
  public double [] getYErrorArray();
}
