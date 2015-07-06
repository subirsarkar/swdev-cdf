package config.svt;

public class RoadEvent extends SvtEvent {
    /** Constructor 
     *  @param data  The event words
     *  @param ee    The End Event word
     */
  public RoadEvent(final int [] data, int ee) {
    super(data, ee);
  }
  public void addObjects(final int [] data) {
    for (int i = 0; i < data.length; i++) 
      addObject(new Road(data[i]));
  }
    /** Get a Road object
     *  @param i Road index
     *  @return i-th Road
     */
  public Road getRoad(int i) {
    return (Road) getObject(i);
  }
  public static void main(String [] argv) {
    int [] words = {0x2e04cc, 0x2e0f31};
    System.out.println(new RoadEvent(words, 0x600085));
  }
}
