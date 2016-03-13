package org.proactive.ui;

import javax.swing.JTree;
import java.util.List;
import java.awt.Component;
import java.util.HashMap;
import org.proactive.prolog.PrologObject;
import org.proactive.prolog.Engine;
import org.proactive.ReactLeafComponent;

public class Tree extends ReactLeafComponent 
{
   JTree tree = new JTree();
   public void setProperties(HashMap<String, PrologObject> properties)
   {
     super.setProperties(properties);
   }
   public Component getAWTComponent()
   {
      return tree;
   }
}
