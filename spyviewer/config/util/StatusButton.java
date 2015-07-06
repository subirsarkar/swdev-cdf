package config.util;

import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.event.*;
import java.awt.MediaTracker;

public class StatusButton extends JRadioButton {
  public static ImageIcon 
    on   = new ImageIcon(AppConstants.iconDir+"green-ball.gif", "on"),
    off  = new ImageIcon(AppConstants.iconDir+"red-ball.gif", "off");
  private String onText   = "On";
  private String offText  = "Off";
  private boolean useText = false;

  public StatusButton(String text, boolean init){
    super(text, new ImageIcon(), init);
    int onStat = on.getImageLoadStatus();
    if (onStat != MediaTracker.COMPLETE) {
      switch (onStat) {
      case MediaTracker.LOADING :
        System.out.println("On Icon Loading");
	break;
      case MediaTracker.ERRORED :
	System.out.println("On Icon Load Error");
	break;
      case MediaTracker.ABORTED :
	System.out.println("On Icon Load Aborted");
	break;
      }
    }
    int offStat = off.getImageLoadStatus();
    if (offStat != MediaTracker.COMPLETE) {
      switch (offStat) {
      case MediaTracker.LOADING :
	System.out.println("Off Icon Loading");
	break;
      case MediaTracker.ERRORED :
	System.out.println("Off Icon Load Error");
	break;
      case MediaTracker.ABORTED :
	System.out.println("Off Icon Load Aborted");
	break;
      }
    }
    setIcon(off);
    setSelectedIcon(on);
  }

  public StatusButton() {
    this("", false);
  }

  public StatusButton(String text) {
    this(text, false);
  }

  public StatusButton(boolean init) {
    this("", init);
  }


 /**
  *  Constructor to use if it is desired that the text changes with the status
  *   @param onText:   Text to display when the Button is Selected
  *   @param offText:  Text to display when the Button is Deselected
  *   @param init:     Initial Status
  */
  public StatusButton(String onText, String offText, boolean init){
    this((init ? onText : offText),init);
    this.onText  = onText;
    this.offText = offText;
    useText = true;
  }

 /**
  *  Sets the state of the button and changes the text.
  *   @param state:     New Status
  */
  public void setSelected(boolean state) {
    super.setSelected(state);
    if (useText) setText((state?onText:offText));
  }

 /**
  * Sets the message to display when the Button is Selected.
  *   @param onText:   Text to display when the Button is Selected
  */
  public void setSelectedText(String onText) {
    this.onText = onText;
    if ((useText)&isSelected()) setText(onText);
  }

 /**
  * Sets the message to display when the Button is Deselected.
  *   @param offText:   Text to display when the Button is Deselected
  */
  public void setDeselectedText(String onText) {
    this.offText = offText;
    if ((useText)&(!isSelected())) setText(offText);
  }


 /**
  * Sets the behaviour of the text label, if it should change with the state.
  *  @param useText: If the text should be changed with the state
  */
  public void setTextUse(boolean useText) {
    this.useText = useText;
  }

  protected void processMouseEvent(MouseEvent e){
    e.consume(); // Eat the Mouse event
  }

  public static void main(String args[]){
    JFrame frme = new JFrame("test");
    JPanel pan1 = new JPanel();
    JButton change;
    final  StatusButton b1,b2,b3,b4, b5;
    frme.getContentPane().add(pan1);
    pan1.add(b1 = new StatusButton());
    pan1.add(b2 = new StatusButton(true));
    pan1.add(b3 = new StatusButton("Third",true));
    pan1.add(b4 = new StatusButton("Fourth"));
    pan1.add(b5 = new StatusButton("On", "Off", true));

    pan1.add(change = new JButton("Change em"));
    change.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	b1.setSelected(!(b1.isSelected())); 
	b2.setSelected(!(b2.isSelected())); 
	b3.setSelected(!(b3.isSelected())); 
	b4.setSelected(!(b4.isSelected())); 
	b5.setSelected(!(b5.isSelected())); 
      }
    });
    frme.setSize(100, 200);
    frme.setVisible(true);
  }
}
