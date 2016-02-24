import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.*;
import java.util.*;


public class ReactApp extends JFrame implements ReactComponent, CodeChangeListener
{
   private Engine engine = null;
   private PrologContext context = null;
   List<ReactComponent> children = new LinkedList<ReactComponent>();
   String URL = null;
   String rootElementId = null;
   public ReactApp(String URL, String rootElementId) throws Exception
   {
      super("React Test");
      engine = new Engine(URL + "/" + rootElementId);
      this.URL = URL;
      this.rootElementId = rootElementId;
      React.addCodeChangeListener(URL, rootElementId, this);
      
      // This is a bit finicky. First we have to set up the state as 'empty'.
      // The empty state is not as empty as you might think. It contains 2 nodes:
      //    * The global root. This is the representation of this JFrame
      //    * Inside this is a RootPanel. This is like the contentPane in the frame
      // Unlike in Swing, we can change the contentPane to a new one by patching it
      // but the global domRoot is immutable. In reality, we should only EVER have one
      // child here, otherwise Swing goes a bit... well, weird.
      
      getContentPane().setBackground(java.awt.Color.GREEN);
      getContentPane().setLayout(new BorderLayout());
      
      ReactComponent contentPane = new RootPanel(rootElementId, engine);
      insertChildBefore(contentPane, null);
      contentPane.getContext().reRender();
   }

   public void handleCodeChange() 
   {
      try
      {
         if (context != null)
         {
            context.getEngine().make();
            context.reRender();
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   public PrologContext getContext() { return context; }
   public void insertChildBefore(ReactComponent child, ReactComponent sibling)
   {      
      if (sibling == null)
      {
         child.setParentNode(this);
         children.add(child);
         getContentPane().add((Component)child, BorderLayout.CENTER);
         context = child.getContext();
      }
      validate();
      repaint();
   }
   public void removeChild(ReactComponent child)
   {
      context = null;
      children.remove(child);
      getContentPane().remove((Component)child);
   }  
   public ReactComponent getParentNode() { return this; }
   public void replaceChild(ReactComponent newNode, ReactComponent oldNode)
   {
      removeChild(oldNode);
      insertChildBefore(newNode, null);
   }
   public void setParentNode(ReactComponent parent) {}
   public ReactComponent getOwnerDocument() { return this; }
   public void setOwnerDocument(ReactComponent owner) {}
   public List<ReactComponent> getChildNodes() { return children; }
   public int getFill() { return 0; }
   public void setProperty(String name, Object value) {}
}
