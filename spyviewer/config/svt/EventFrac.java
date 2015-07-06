package config.svt;

/** Define  a structure to hold SVT words */
public class EventFrac extends SvtEvent {
  private String bufferType;
  /** 
   * Simple initialisation of the object
   *
   * @param  words      SVTword array
   * @param  endEvent   End event word
   */
  public EventFrac(final int [] words, int ee, final  String bufferType) {
    super(words, ee);
    this.bufferType = bufferType;
    // Create specific objects
  }
  public void addObjects(final int [] words) {
  }
  public String getBufferType() {
    return bufferType;
  }
}
