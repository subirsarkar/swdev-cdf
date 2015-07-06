package config.util;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Timex {
  GregorianCalendar cal;
  PrintfFormat d2 = AppConstants.d2Format; 
  PrintfFormat d4 = AppConstants.d4Format; 
  public Timex() {
    cal = new GregorianCalendar();
  }
  public String toString() {
    Date d = new Date();
    cal.setTime(d);

    int y = cal.get(Calendar.YEAR);
    int m = cal.get(Calendar.MONTH);
    int a = cal.get(Calendar.DAY_OF_MONTH);
    int h = cal.get(Calendar.HOUR);
    int t = cal.get(Calendar.MINUTE);
    int s = cal.get(Calendar.SECOND);

    StringBuilder buf = new StringBuilder();
    buf.append(d4.sprintf(y))
       .append(d2.sprintf(m))
       .append(d2.sprintf(a))
       .append(d2.sprintf(h))
       .append(d2.sprintf(t))
       .append(d2.sprintf(s));

    return buf.toString();
  }
}
