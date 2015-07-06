package config.svt;

import config.util.AppConstants;
import config.util.PrintfFormat;

public class EEWord {
  private int word;
  public EEWord() {
    this(0);
  }
  public EEWord(int word) {
    setWord(word);
  }
  public void setWord(int word) {
    this.word = word;
  }
  public int getWord() {
    return word;
  }
  public static String getHtmlLabels() {
    return getHtmlLabels("MRG");  // Anything but GB will do
  }
  public static String getHtmlLabels(final String boardName) {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    final String [] tags = 
    {
	"L2 Buffer ID", 
        "L1 Trigger Info",
        "Spare",
        "Lost G-link",
        (boardName.equals("GB") ? "No. of good tracks &gt;1" :"Truncated Output"),
        (boardName.equals("GB") ? "No. of good tracks &gt;0" :"Internal Overflow"),
        "Invalid Data",
        "FIFO Overflow",
        "Lost Sync",
        "Parity Error",
        "Parity Bit",
        "Event Tag"
    };

    buf.append("<TR ALIGN=\"RIGHT\">");
    for (int i = 0; i < tags.length; i++)
      buf.append("<TD>").append(tags[i]).append("</TD>");
    buf.append("</TR>");

    return buf.toString();
  }
  public String getHtmlData() {
    return getHtmlData(word);
  }
  public static String getTextLabels() {
    return getTextLabels("MRG"); // Anything but GB will do
  }
  // Is it static enough? Looks like we can be more efficient
  // Investigate
  public static String getTextLabels(final String boardName) {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    PrintfFormat format = AppConstants.s4Format;
     
    String [] tags = 
    {
	"L2B", "L1T", "E2", "GL", (boardName.equals("GB") ? "TC2" : "TO"),
        (boardName.equals("GB") ? "TC1" : "IO"), "ID","FO","LS","PE","PB","ET"
    };
    for (int i = 0; i < tags.length; i++) {
	buf.append(format.sprintf(tags[i]));
    }
    buf.append("\n");

    return buf.toString();
  }
  public static String getTextData(int word) {
    PrintfFormat format = AppConstants.d4Format;
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    buf.append(format.sprintf(word >> 19 & 0x3))
       .append(format.sprintf(word >> 17 & 0x3));
    for (int i = 16; i > 7; i--) 
      buf.append(format.sprintf(word >> i & 0x1));
    buf.append(format.sprintf(word >> 0 & AppConstants.MASK08))
       .append("\n");

    return buf.toString();
  }
  public static String getHtmlData(int word) {
    StringBuilder buf = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    buf.append("<TR ALIGN=\"RIGHT\">");
    buf.append("<TD>").append(word >> 19 & 0x3).append("</TD>").
        append("<TD>").append(word >> 17 & 0x3).append("</TD>");
    for (int i = 16; i > 7; i--) 
      buf.append("<TD>").append(word >> i & 0x1).append("</TD>");
    buf.append("<TD>").append(word >>  0 & AppConstants.MASK08).append("</TD>").
    append("</TR>");

    return buf.toString();
  }
  public String toString() {
    return getHtmlData();
  }
  public static void main(String [] argv) {
    int [] words = {0x600fff, 0x654de3, 0x766fee};
    System.out.print(EEWord.getTextLabels("GB"));
    for (int i = 0; i < words.length; i++) {
      System.out.print(EEWord.getTextData(words[i]));
    }
  }
}
