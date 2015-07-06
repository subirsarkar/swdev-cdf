package config.util;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

public class MyTreeModelListener implements TreeModelListener {
  private static final boolean DEBUG = false;
  private DefaultTreeModel treeModel;
  public MyTreeModelListener(DefaultTreeModel treeModel) {
    this.treeModel = treeModel;
  }
  public void treeNodesChanged(TreeModelEvent e) {
  }
  public void treeNodesInserted(TreeModelEvent e) {
    if (DEBUG) System.out.println("MyTreeModelListener -> new Node inserted!"); 
    //    treeModel.reload();
  }
  public void treeNodesRemoved(TreeModelEvent e) {
  }
  public void treeStructureChanged(TreeModelEvent e) {
    if (DEBUG) System.out.println("MyTreeModelListener -> tree structure changed!"); 
    //    treeModel.reload();
  }
}
