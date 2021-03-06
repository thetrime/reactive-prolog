package org.proactive.ui;

import org.proactive.prolog.PrologObject;
import org.proactive.ReactComponent;
import org.proactive.ReactComponentFactory;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.JViewport;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;

public class Panel extends ReactComponent 
{
   private static final int HORIZONTAL = 0;
   private static final int VERTICAL = 1;
   private static final int GRID = 2;
   int nextIndex = 0;
   int orientation = VERTICAL;
   ProactiveConstraints.Alignment alignment = ProactiveConstraints.Alignment.STRETCH;
   ProactiveConstraints.Justification justification = ProactiveConstraints.Justification.START;
   int total_x_weight = 0;
   int total_y_weight = 0;
   private ProactiveLayoutManager.Wrap wrap = ProactiveLayoutManager.Wrap.NOWRAP;
   private LayoutManager layoutManager;
   private Component awtComponent;
   private JPanel panel;
   private String id;
   private static int global_id = 0;
   private String scrollInfo = "none";
   private JScrollPane scroll = null;
   LinkedList<ReactComponent> childComponents = new LinkedList<ReactComponent>();

   private class ScrollablePanel extends JPanel implements Scrollable
   {
      public int getScrollableBlockIncrement(Rectangle visibleRect,
                                             int orientation,
                                             int direction)
      {
         // I'm not really sure how to implement this
         JViewport vp;
         if (getParent() instanceof JViewport)
         {
            vp = (JViewport)getParent();
            if (orientation == VERTICAL)
               return vp.getExtentSize().height;
            return vp.getExtentSize().width;
         }
         return 5;
      }
      public int getScrollableUnitIncrement(Rectangle visibleRect,
                                            int orientation,
                                            int direction)
      {
         // I'm not sure how to implement this
         return 1;
      }
      public Dimension getPreferredScrollableViewportSize()
      {
         return getPreferredSize();
      }
      public Dimension getPreferredSize()
      {
         return super.getPreferredSize();
      }
      public Dimension getMinimumSize()
      {
         Dimension m = super.getMinimumSize();
         if ("none".equals(scrollInfo))
            return m;
         else if ("vertical".equals(scrollInfo))
            return new Dimension((int)m.getWidth(), 0);
         else if ("horizontal".equals(scrollInfo))
            return new Dimension(0, (int)m.getHeight());
         return new Dimension(0, 0);
      }
      public boolean getScrollableTracksViewportHeight()
      {
         // First of all, if the panel is set to not scroll, or only scroll vertically, then we must always try and squash the scrollable
         // into the height of the scrollpane
         if ("horizontal".equals(scrollInfo) || "none".equals(scrollInfo))
            return true;
         // Next, we must be sometimes trying to scroll vertically. But if the viewport is bigger than the scrollable then return true anyway
         // otherwise we will have blank space at the bottom
         if (getParent() instanceof JViewport && ((JViewport)getParent()).getHeight() > getPreferredSize().height)
            return true;
         // The scrollable really is taller than the viewport. Let it scroll
         return false;
      }
      public boolean getScrollableTracksViewportWidth()
      {
         // See getScrollableTracksViewportHeight for an explanation, swapping width/height for this
         if ("vertical".equals(scrollInfo) || "none".equals(scrollInfo))
             return true;
         if (getParent() instanceof JViewport && ((JViewport)getParent()).getWidth() > getPreferredSize().width)
            return true;
         return false;
      }

   }

   public Panel()
   {
      super();
      panel = new ScrollablePanel();
      this.id = "{" + (global_id++) + "}";
      awtComponent = panel;
      panel.setBackground(new Color(150, 168, 200));
      reconfigureLayout();
      //panel.setBorder(BorderFactory.createLineBorder(Color.RED));
   }

   private void reconfigureLayout()
   {
      if (orientation == HORIZONTAL)
         layoutManager = new ProactiveLayoutManager(layoutManager, ProactiveLayoutManager.HORIZONTAL, alignment, justification, wrap);
      else if (orientation == VERTICAL)
         layoutManager = new ProactiveLayoutManager(layoutManager, ProactiveLayoutManager.VERTICAL, alignment, justification, wrap);
      panel.setLayout(layoutManager);
   }


   public void setProperties(HashMap<String, PrologObject> properties)
   {
      super.setProperties(properties);
      if (properties.containsKey("key"))
      {
         id = properties.get("key").asString();
      }
      if (properties.containsKey("label"))
      {
         if (properties.get("label").isNull())
            panel.setBorder(null);
         else
         {
            java.awt.Font font = new java.awt.Font("Arial", 0, 11);
            java.awt.Color colour = java.awt.Color.WHITE;
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(colour),
                                                                                                BorderFactory.createEmptyBorder(0, 0, 0, 0)),
                                                             properties.get("label").asString(),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.TOP,
                                                             font,
                                                             colour));
         }
      }
      if (properties.containsKey("layout"))
      {
         int oldOrientation = orientation;
         if (properties.get("layout") == null)
         {
            orientation = VERTICAL;
         }
         else
         {
            String key = properties.get("layout").asOrientation();
            if (key.equals("vertical"))
               orientation = VERTICAL;
            else if (key.equals("horizontal"))
               orientation = HORIZONTAL;
            else if (key.equals("grid"))
               orientation = GRID;
            else
               orientation = VERTICAL;
         }
         if (orientation != oldOrientation)
         {
            if (orientation != GRID)
            {
               reconfigureLayout();
            }
            else if (oldOrientation != GRID && orientation == GRID)
            {
               int rows = 0;
               int cols = 0;
               if (properties.containsKey("cols"))
                  cols = properties.get("cols").asInteger();
               if (properties.containsKey("rows"))
                  rows = properties.get("rows").asInteger();
               layoutManager = new GridLayout(rows, cols);
               panel.setLayout(layoutManager);
            }
            repackChildren();
         }
      }
      if (properties.containsKey("align-children"))
      {
         ProactiveConstraints.Alignment oldAlignment = alignment;
         if (properties.get("align-children") == null)
         {
            alignment = ProactiveConstraints.Alignment.START;
         }
         else
         {
            String key = properties.get("align-children").asString();
            if (key.equals("start"))
                alignment = ProactiveConstraints.Alignment.START;
            else if (key.equals("center"))
               alignment = ProactiveConstraints.Alignment.CENTER;
            else if (key.equals("end"))
               alignment = ProactiveConstraints.Alignment.END;
            else if (key.equals("stretch"))
               alignment = ProactiveConstraints.Alignment.STRETCH;
         }
         if (oldAlignment != alignment)
            reconfigureLayout();
      }
      if (properties.containsKey("justify-content"))
      {
         ProactiveConstraints.Justification oldJustification = justification;
         if (properties.get("justify-content") == null)
         {
            justification = ProactiveConstraints.Justification.CENTER;
         }
         else
         {
            String key = properties.get("justify-content").asString();
            if (key.equals("start"))
                justification = ProactiveConstraints.Justification.START;
            else if (key.equals("center"))
               justification = ProactiveConstraints.Justification.CENTER;
            else if (key.equals("end"))
               justification = ProactiveConstraints.Justification.END;
            else if (key.equals("space-between"))
               justification = ProactiveConstraints.Justification.SPACE_BETWEEN;
            else if (key.equals("space-around"))
               justification = ProactiveConstraints.Justification.SPACE_AROUND;
         }
         if (oldJustification != justification)
            reconfigureLayout();
      }
      if (properties.containsKey("wrap"))
      {
         ProactiveLayoutManager.Wrap oldWrap = wrap;
         if (properties.get("wrap") == null)
         {
            wrap = ProactiveLayoutManager.Wrap.NOWRAP;
         }
         else
         {
            String key = properties.get("wrap").asString();
            if (key.equals("nowrap"))
               wrap = ProactiveLayoutManager.Wrap.NOWRAP;
            else if (key.equals("wrap"))
               wrap = ProactiveLayoutManager.Wrap.WRAP;
            else if (key.equals("wrap-reverse"))
               wrap = ProactiveLayoutManager.Wrap.WRAP_REVERSE;
         }
         if (oldWrap != wrap)
            reconfigureLayout();
      }
      if (properties.containsKey("scroll"))
      {
         // FIXME: Not this simple!
         //   * Check scroll modes
         //   * Could be turning scroll ON->OFF!
         //   * Could be changing from scroll->different scroll
         if (properties.get("scroll") == null || "none".equals(properties.get("scroll")))
         {
            scrollInfo = "none";
            scroll = null;
            awtComponent = panel;
         }
         else
         {
            String key = properties.get("scroll").asScroll();
            scrollInfo = key;
            scroll = new JScrollPane(panel);
            if (key.equals("vertical"))
            {
               scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
               scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            }
            else if (key.equals("horizontal"))
            {
               scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
               scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            }
            else if (key.equals("both"))
            {
               scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
               scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            }
            awtComponent = scroll;
         }
         if (getParentNode() != null)
            getParentNode().replaceChild(this, this);
      }
   }
   public void insertChildBefore(ReactComponent child, ReactComponent sibling)
   {
      // First rehome the child in the document
      if (child.getParentNode() != null)
         child.getParentNode().removeChild(child);
      child.setParentNode(this);
      awtMap.put(child, child.getAWTComponent());
      int index = (sibling==null)?-1:children.indexOf(sibling);
      if (index != -1)
      {
         children.add(index, child);
         repackChildren();
      }
      else
      {
         children.add(child);
         addChildToDOM(nextIndex, child);
         nextIndex++;
      }

      child.setOwnerDocument(owner);
   }

   private void addChildToDOM(int index, ReactComponent child)
   {
      if (orientation == GRID)
      {
         if (!(child.getAWTComponent() instanceof JFrame))
            panel.add(child.getAWTComponent(), index);
      }
      else
      {
         if (!(child.getAWTComponent() instanceof JFrame))
         {
            childComponents.add(child);
            panel.add(child.getAWTComponent(), new ProactiveConstraints(child.getFill(), child.getSelfAlignment(), index));
         }
      }
   }

   @Override
   public ProactiveConstraints.Alignment getSelfAlignment()
   {
      return alignment;
   }
   
   private void repackChildren()
   {
      panel.removeAll();
      childComponents.clear();
      int index = 0;
      for (ReactComponent child: children)
         addChildToDOM(index++, child);
   }


   public void removeChild(ReactComponent child)
   {
      // FIXME: This is wallpaper. We should not be removing children from things that are not their parents
      //        I suspect this happens in things like Frame where adding a child to the Frame actually adds it
      //        to the Panel inside the Frame
      if (!children.contains(child))
         return;
      child.setOwnerDocument(null);
      children.remove(child);
      childComponents.remove(child);
      child.setParentNode(null);
      awtMap.remove(child);
      panel.remove(child.getAWTComponent());
   }

   public void replaceChild(ReactComponent newChild, ReactComponent oldChild)
   {
      int i = children.indexOf(oldChild);
      children.set(i, newChild);
      Component oldComponent = awtMap.get(oldChild);
      childComponents.remove(oldChild);
      if (oldChild != newChild)
      {
         oldChild.setOwnerDocument(null);
         oldChild.setParentNode(null);
         newChild.setOwnerDocument(owner);
         newChild.setParentNode(this);
         awtMap.remove(oldChild);
         awtMap.put(newChild, newChild.getAWTComponent());
      }
      if (orientation == VERTICAL || orientation == HORIZONTAL)
      {
         ProactiveConstraints constraints = ((ProactiveLayoutManager)layoutManager).getConstraints(oldComponent);
         // We cannot call removeChild here since the list of children will get truncated
         // and we want to swap in-place
         panel.remove(oldComponent);
         // We may have to edit the constraints if the child has a different fill
         constraints.fill = newChild.getFill();
         if (!(newChild.getAWTComponent() instanceof JFrame))
         {
            panel.add(newChild.getAWTComponent(), constraints);
         }
      }
      else if (orientation == GRID)
      {
         panel.remove(oldComponent);
         if (!(newChild.getAWTComponent() instanceof JFrame))
         {
            panel.add(newChild.getAWTComponent(), i);
         }
      }
   }

   public Component getAWTComponent()
   {
      return awtComponent;
   }

   public String toString()
   {
      if (id != null)
         return "(Panel: " + id + ", " + children + ")";
      return "(Panel " + children + ")";
   }
}
