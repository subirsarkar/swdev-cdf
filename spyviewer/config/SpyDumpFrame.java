package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import config.util.*;
/*
 * @version  0.1, July 2000
 * @author   S. Sarkar
 *   
 */
public class SpyDumpFrame extends DataFrame {
  public SpyDumpFrame(String label) {
    super(false, label, true);
    getJMenuBar().getMenu(0).getItem(0).setEnabled(true);
  }
  public static void main(String [] argv) {
    JFrame f = new SpyDumpFrame("Data Display");
    f.setSize(600, 600);
    f.setVisible(true);
  }
}
