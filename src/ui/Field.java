package org.proactive.ui;

import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import java.util.List;
import java.util.HashMap;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import org.proactive.prolog.PrologObject;
import org.proactive.ReactComponent;

public class Field extends ReactComponent
{
   private static final int TEXT = 0;
   private static final int RADIO = 1;
   private static final int CHECKBOX = 2;
   private static final int PASSWORD = 3;

   private PopupMenu popup = null;
   private InputWidget widget;
   private int type = TEXT;
   private int maxWidth = -1;
   public Field()
   {
      widget = new TextField();
   }

   private PrologObject serializeObject()
   {
      HashMap<String, Object> properties = new HashMap<String, Object>();
      properties.put("value", getValue());
      return PrologObject.serialize(properties);
   }

   private FocusListener focusListener = null;
   private void setFocusListener(PrologObject value)
   {
      if (focusListener != null)
         widget.getAWTComponent().removeFocusListener(focusListener);
      if (value == null || value.isNull())
         return;
      focusListener = new FocusListener()
         {
            public void focusLost(FocusEvent fe)
            {
               try
               {
                  getOwnerDocument().triggerEvent(value.asTerm(), serializeObject().asTerm());
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }
            }
            public void focusGained(FocusEvent fe)
            {
            }
         };
      widget.getAWTComponent().addFocusListener(focusListener);
   }

   private MouseListener contextMenuListener = null;
   private void setContextMenuListener(PrologObject value)
   {
      if (contextMenuListener != null)
         widget.getAWTComponent().removeMouseListener(contextMenuListener);
      if (value == null || value.isNull())
         return;
      contextMenuListener = new MouseAdapter()
         {
            public void triggerPopup()
            {
               try
               {
                  getOwnerDocument().triggerEvent(value.asTerm(), serializeObject().asTerm());
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }
            }

            public void mouseClicked(MouseEvent me)
            {
               if (me.isPopupTrigger())
               {
                  triggerPopup();
               }
            }
            public void mousePressed(MouseEvent me)
            {
               if (me.isPopupTrigger())
               {
                  triggerPopup();
               }
            }
         };
      widget.getAWTComponent().addMouseListener(contextMenuListener);
   }

   private MouseListener internalPopupListener = null;
   public void setContextMenuRenderer(PrologObject value)
   {
      if (internalPopupListener != null)
         widget.getAWTComponent().removeMouseListener(internalPopupListener);
      if (value.isNull())
         return;
      internalPopupListener = new MouseAdapter()
         {
            public void createPopup(MouseEvent me)
            {
               try
               {
                  popup = (PopupMenu)getOwnerDocument().renderContextualElement(value.asTerm(), serializeObject().asTerm());
                  popup.getMenu().show(widget.getAWTComponent(), me.getX(), me.getY());
               }
               catch(Exception e)
               {
                  e.printStackTrace();
               }
            }
            public void mouseClicked(MouseEvent me)
            {
               if (me.isPopupTrigger())
               {
                  createPopup(me);
               }
            }
            public void mousePressed(MouseEvent me)
            {
               if (me.isPopupTrigger())
               {
                  createPopup(me);
               }
            }
         };
      widget.getAWTComponent().addMouseListener(internalPopupListener);
   }

   public void setProperties(HashMap<String, PrologObject> properties)
   {
      super.setProperties(properties);
      if (properties.containsKey("type"))
      {
         int oldType = type;
         String key = properties.get("type").asString();
         if (key == null)
            type = TEXT;
         else if (key.equals("text"))
            type = TEXT;
         else if (key.equals("radio"))
            type = RADIO;
         else if (key.equals("checkbox"))
            type = CHECKBOX;
         else if (key.equals("password"))
            type = PASSWORD;
         if (oldType != type)
         {
            if (internalPopupListener != null)
               widget.getAWTComponent().removeMouseListener(internalPopupListener);
            switch(type)
            {
               case TEXT:
                  widget = new TextField();
                  break;
               case PASSWORD:
                  widget = new PasswordField();
                  break;
               case RADIO:
                  widget = new RadioButton();
                  break;
               case CHECKBOX:
                  widget = new CheckBox();
                  break;
            }
            if (maxWidth != -1)
               widget.getAWTComponent().setMaximumSize(new Dimension(maxWidth, (int)widget.getAWTComponent().getPreferredSize().getHeight()));
            if (internalPopupListener != null)
               widget.getAWTComponent().addMouseListener(internalPopupListener);
            if (getParentNode() != null)
               getParentNode().replaceChild(this, this);
         }
      }
      if (properties.containsKey("value"))
      {
         if (properties.get("value") == null)
            setValue(null);
         else
            setValue(properties.get("value"));
      }
      if (properties.containsKey("maxWidth"))
      {
         PrologObject t = properties.get("maxWidth");
         if (t == null || t.isNull())
         {
            maxWidth = -1;
            widget.getAWTComponent().setMaximumSize(null);
         }
         else
         {
            String s = t.asString();
            maxWidth = -1;
            if (s.endsWith("em"))
            {
               //maxWidth = (int)(Integer.parseInt(s.substring(0, s.length()-2)) * (widget.getAWTComponent().getFont().getSize() / (java.awt.Toolkit.getDefaultToolkit().getScreenResolution() / 72.0)));
               int em = (((javax.swing.JComponent)widget.getAWTComponent()).getFontMetrics(widget.getAWTComponent().getFont()).charWidth('M'));
               maxWidth = (int)(Integer.parseInt(s.substring(0, s.length()-2)) * em);
            }
            else if (s.endsWith("px"))
               maxWidth = Integer.parseInt(s.substring(0, s.length()-2));
            else if (s.equals("inherit"))
               maxWidth = -1;
            else
               System.out.println("Bad maxWidth: " + s);
            int height = (int)widget.getAWTComponent().getPreferredSize().getHeight();
            if (maxWidth != -1)
            {
               //System.out.println("Setting max-width to " + new Dimension(maxWidth, height));
               widget.getAWTComponent().setMaximumSize(new Dimension(maxWidth, height));
            }
         }
      }
      if (properties.containsKey("onBlur"))
         setFocusListener(properties.get("onBlur"));
      if (properties.containsKey("disabled"))
         widget.setDisabled(properties.get("disabled").asBoolean());
      if (properties.containsKey("renderContextMenu"))
         setContextMenuRenderer(properties.get("renderContextMenu"));
      if (properties.containsKey("verifyValue"))
      {
         PrologObject handler = properties.get("verifyValue");
         if (handler == null || handler.isNull())
            widget.setVerifier(null);
         else
            widget.setVerifier(new InputWidgetVerifier()
               {
                  public boolean verifyValue(PrologObject newValue)
                  {
                     try
                     {
                        return getOwnerDocument().triggerTest(handler.asTerm(), newValue.asTerm());
                     }
                     catch(Exception e)
                     {
                        e.printStackTrace();
                     }
                     return false;
                  }
               });
      }
      if (properties.containsKey("onContextMenu"))
         setContextMenuListener(properties.get("onContextMenu"));
      if (properties.containsKey("onChange"))
      {
         PrologObject handler = properties.get("onChange");
         if (handler == null || handler.isNull())
            widget.setChangeListener(null);
         else
            widget.setChangeListener(new InputWidgetListener()
               {
                  public void valueWouldChange(PrologObject newValue)
                  {
                     try
                     {
                        getOwnerDocument().triggerEvent(handler.asTerm(), newValue.asTerm());
                     }
                     catch(Exception e)
                     {
                        e.printStackTrace();
                     }
                  }
               });
      }
      if (properties.containsKey("align"))
	 setAlignment(properties.get("align"));
   }
   public void setAlignment(PrologObject value)
   {
      int alignment = JTextField.LEFT;
      if (!value.isNull())
      {
	 if ("left".equals(value.asString()))
	    alignment = JTextField.LEFT;
	 else if ("right".equals(value.asString()))
	    alignment = JTextField.RIGHT;
      }
      widget.setAlignment(alignment);

   }

   public class MenuItem extends ReactComponent
   {
      public Component getAWTComponent() { return null; }
   }

   public void insertChildBefore(ReactComponent child, ReactComponent sibling)
   {
      super.insertChildBefore(child, sibling);
   }


   public void removeChild(ReactComponent child)
   {
      super.removeChild(child);
   }

   public void replaceChild(ReactComponent newChild, ReactComponent oldChild)
   {
      super.replaceChild(newChild, oldChild);
   }

   public void setValue(PrologObject value)
   {
      widget.setValue(value);
   }

   public Object getValue()
   {
      return widget.getValue();
   }

   public Component getAWTComponent()
   {
      return widget.getAWTComponent();
   }
}
