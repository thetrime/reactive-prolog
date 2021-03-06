package org.proactive.ui;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.HashMap;
import java.awt.Component;
import java.util.HashMap;
import org.proactive.prolog.PrologObject;
import org.proactive.prolog.Engine;
import org.proactive.ReactComponent;

public class Tree extends ReactComponent
{
   DefaultTreeModel model = null;
   DefaultMutableTreeNode node;
   JTree tree = null;
   public Tree()
   {
      node = new DefaultMutableTreeNode("");
      model = new DefaultTreeModel(node);
      tree = new JTree(model);
   }
   public void setProperties(HashMap<String, PrologObject> properties)
   {
     super.setProperties(properties);
     if (properties.containsKey("label"))
     {
        node.setUserObject(properties.get("label").asString());
        model.nodeChanged(node);
     }
   }
   public Component getAWTComponent()
   {
      return tree;
   }

   public void insertChildBefore(ReactComponent child, ReactComponent sibling)
   {
      super.insertChildBefore(child, sibling);
      if (child instanceof TreeNode)
      {
         ((TreeNode)child).setModel(model);
         int index = 0;
         if (sibling != null)
            index = node.getIndex(((TreeNode)sibling).getNode());
         node.insert(((TreeNode)child).getNode(), index);
         model.nodeStructureChanged(node);
      }
   }

   public void replaceChild(ReactComponent newChild, ReactComponent oldChild)
   {
      int index = node.getIndex(((TreeNode)oldChild).getNode());
      super.replaceChild(newChild, oldChild);
      node.remove(((TreeNode)oldChild).getNode());
      node.insert(((TreeNode)newChild).getNode(), index);
      model.nodeStructureChanged(node);
   }

   public void removeChild(ReactComponent child)
   {
      super.removeChild(child);
      if (child instanceof TreeNode)
      {
         node.remove(((TreeNode)child).getNode());
         model.nodeStructureChanged(node);
      }
   }

}
