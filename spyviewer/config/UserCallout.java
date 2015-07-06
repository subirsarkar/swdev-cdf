package config;

import java.awt.*;
/**
 * Handles RC commands. The messages are printed on the RC command
 * window from this class. Object of this class has a handle to the 
 * main class which is used to set client state in response to RC
 * command if the client becomes a part of the partition which includes
 * the Run Control.
 *
 * @author S. Sarkar
 * @version 0.1, September 2000
 */
public interface UserCallout {

    /** 
     *  Respond to R_C state transition commands by setting new active state,
     *  printing the transition message etc.
     * 
     *  @param state  RC State
     *  @param value  Value which the active state will assume now 
     *  @param color  Text color which depends on the type of RC State (reset, end etc.)
     */
  public void callout(final String state, int value, Color color);
    /** 
     *  Set new active state
     *  @param state  RC State
     */
  public void setActiveState(int state);
}
