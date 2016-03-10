package org.proactive.vdom;
import org.proactive.ReactComponent;
import org.proactive.ReactComponentFactory;

public class ReactEditInsert extends ReactEdit
{
   public ReactEditInsert(PrologNode node)
   {      
      super(node);
   }

   public ReactComponent apply(ReactComponent domNode) throws Exception
   {
      /*
      if (domNode != null)
      {      
         ReactComponent newNode = ReactComponentFactory.instantiateNode(node, domNode.getContext());
         domNode.insertChildBefore(newNode, null);
      }
      return domNode;
      */
      return null;
   }
   
   public String toString()
   {
      return "<Insert element: " + node + ">";
   }
}
