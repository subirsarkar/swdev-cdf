package config.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.ListSelectionModel;
 
public class RemoteFileDialog extends JDialog {
  public String [] items;
  private static final Font cfont = new Font("SansSerif", Font.PLAIN, 12);
  private static final ImageIcon fcIcon = new ImageIcon(AppConstants.iconDir+"closed.gif");
  private static final ImageIcon fsIcon = new ImageIcon(AppConstants.iconDir+"opened.gif");
  private final Dimension bsize = new Dimension(90, 25);
  private static final Color mblue = new Color(205, 205, 255);

  private JPanel panel = new JPanel(new BorderLayout()),
           buttonPanel = new ButtonPanel();
  private    InputPanel inputPanel = new InputPanel();
  private FileListPanel  listPanel = new FileListPanel();
  
  public RemoteFileDialog() {
    setTitle("Select Remote File");
    setModal(true);
      
    // Top Level
    panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    panel.add(inputPanel, BorderLayout.NORTH);
    panel.add(buttonPanel, BorderLayout.SOUTH);
      
    getContentPane().add(panel, BorderLayout.CENTER);

    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }
  // Panel that contains the input text field + selection options 
  class InputPanel extends JPanel {
    JButton badd = new JButton("Add");
    JTextField inputField = new JTextField();
    JRadioButton [] rb;
    InputPanel() {
      super(true);
      setLayout(new BorderLayout());

      // Input area
      JPanel textPanel = new JPanel(new BorderLayout());
      JLabel label = Tools.createLabel("URL:");
      label.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));

      inputField.setPreferredSize(new Dimension(500, 20));
      inputField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          setData();
        }
      });
      
      badd.setPreferredSize(bsize);
      badd.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          setData();
        }
      });
      badd.setEnabled(false);

      textPanel.add(label, BorderLayout.WEST);
      textPanel.add(inputField, BorderLayout.CENTER);
      textPanel.add(badd, BorderLayout.EAST);
      add(textPanel, BorderLayout.NORTH);

      // Options
      JPanel optionPanel = new JPanel(new BorderLayout());
      add(optionPanel, BorderLayout.CENTER);

      JPanel option2Panel = new JPanel(new FlowLayout());
      label = Tools.createLabel("Selection Mode:");
      label.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
      option2Panel.add(label);

      ButtonGroup group = new ButtonGroup();
      String [] options  = {"Single", "Multiple"};
      rb = new JRadioButton[options.length];
      for (int i = 0; i < rb.length; i++) {
        rb[i] = Tools.radioButton(options[i], options[i], group, ((i==0)?true:false));
        rb[i].setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 0));
        option2Panel.add(rb[i]);
      }
      rb[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          badd.setEnabled(false);
          panel.remove(listPanel);        
          pack();
        }
      });
      rb[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          badd.setEnabled(true);
          panel.add(listPanel, BorderLayout.CENTER);
          pack();
        }
      });
      option2Panel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
      optionPanel.add(option2Panel, BorderLayout.EAST);
    }
    public boolean isSingleSelection() {
      return rb[0].isSelected();
    }
    public String getText() {
      return inputField.getText();
    }
    public void setData() {
      listPanel.setItem(inputField.getText());         
      inputField.setText(""); 
    }
  }
  class FileListPanel extends JPanel {
    ListViewPanel lvPanel = new ListViewPanel();
    FileListPanel() {
      super(true);
      setLayout(new BorderLayout());

      JPanel aPanel = new JPanel();
      aPanel.setPreferredSize(new Dimension(35, 400));
      aPanel.setBackground(mblue);
      add(aPanel, BorderLayout.WEST);
    
      lvPanel.setPreferredSize(new Dimension(450, 400));
      add(lvPanel, BorderLayout.CENTER);

      JPanel bc = new JPanel(new BorderLayout());

      JPanel bPanel = new JPanel(new GridLayout(0,1));
      bc.add(bPanel, BorderLayout.NORTH);

      JButton brem = new JButton("Remove");
      brem.setPreferredSize(bsize);
      brem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
	  lvPanel.removeItems();
        }
      });
      bPanel.add(brem);

      JButton bclear = new JButton("Clear");
      bclear.setPreferredSize(bsize);
      bclear.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
	  lvPanel.clearList();
        }
      });
      bPanel.add(bclear);

      JPanel cPanel = new JPanel();
      cPanel.setPreferredSize(new Dimension(30, 400));
      cPanel.setBackground(mblue);
      bc.add(cPanel, BorderLayout.CENTER);

      add(bc, BorderLayout.EAST);
    }
    void setItem(final String item) {
      lvPanel.setItem(item);
    }
    public void clearList() {
      lvPanel.clearList();
    }
    public void removeItems() {
      lvPanel.removeItems();
    }
    public String [] getItems() {
      return lvPanel.getItems();
    }
  }
  class ButtonPanel extends JPanel {
    ButtonPanel() {
      setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
      
      JButton bok = new JButton("OK");
      bok.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          if (inputPanel.isSingleSelection()) {
            if (inputPanel.getText().length() == 0)
              items = new String[0];
            else {
              items = new String[1];
              items[0] = inputPanel.getText();
            }
          }
          else {
            listPanel.getItems();
          }
          RemoteFileDialog.this.dispose();
        }
      });
      add(bok);
      
      JButton bcan = new JButton("Cancel");
      bcan.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          items = new String[0];
          RemoteFileDialog.this.dispose();
        }
      });
      add(bcan);
      bok.setPreferredSize(bcan.getPreferredSize());
    }
  }
  class ListViewPanel extends JPanel {
    protected MutableList list;
    MyCellRenderer renderer;
    ListViewPanel() {
      super(true);
      setLayout(new BorderLayout());

      // List View
      renderer = new MyCellRenderer(fsIcon, fcIcon);
      list = new MutableList();
      list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      list.setSelectionBackground(new Color(200,200,255));
      list.setBackground(Color.white);
      list.setFont(cfont);
      list.setCellRenderer(renderer);
      list.setVisibleRowCount(6);

      JScrollPane scp = new JScrollPane(list);
      add(scp, BorderLayout.CENTER);
    }  
    public void clearList() {
      DefaultListModel model = list.getContents();
      model.clear();
    }
    public void removeItems() {
      DefaultListModel model = list.getContents();
      int [] indices = list.getSelectedIndices(); 
      for (int i = 0; i < indices.length; i++) {
	model.removeElementAt(i);
      }
    }
    public void setItem(final String item) {
      DefaultListModel model = list.getContents();
      model.addElement(item);
    }
    public String [] getItems() {
      DefaultListModel model = list.getContents();
      items = new String[model.getSize()];
      model.copyInto(items); 
      return items;
    }
  }
  public static void main(String[] args) {
    RemoteFileDialog dlg = new RemoteFileDialog();
    String [] items = dlg.items;
    for (int i = 0; i < items.length; i++) {
      System.out.println(items[i]);
    }
  }
}
