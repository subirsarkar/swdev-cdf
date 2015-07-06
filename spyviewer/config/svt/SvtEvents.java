package config.svt;

import java.util.Vector;

public interface SvtEvents {
  public Vector getEvents();
  public SvtEvent getEvent(int i);
  public void buildEvents(final int [] data);
  public int [] getEventTags();
  public int [] getEEWords();
  public int getNumberOfEvents();
}
