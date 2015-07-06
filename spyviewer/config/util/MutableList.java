package config.util;

import javax.swing.JList;

import javax.swing.DefaultListModel;

public class MutableList extends JList {
  public MutableList() {
    super(new DefaultListModel());
  }
  public DefaultListModel getContents() {
    return (DefaultListModel)getModel();
  }
}   
