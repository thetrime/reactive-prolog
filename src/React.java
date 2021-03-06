package org.proactive;

//https://github.com/Matt-Esch/virtual-dom

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.net.URI;
import org.proactive.prolog.Engine;
import gnu.prolog.term.Term;

public class React
{
   // Prevent instantiation
   private React() {}
   private static String requestedLaf = null;
   public static String currentLaf = null;
   private static String uri = null;
   private static String component = null;
   private static Map<String, String> httpHeaders;

   public static void main(String[] args) throws Exception
   {
      List<String> cookies = new LinkedList<String>();
      if (args.length < 2)
      {
         if (args.length == 1 && args[0].equals("--test"))
         {
            Engine.runTests();
            System.exit(-1);
         }
         System.err.println("Usage: React [Options] <URI> <Component>");
         System.err.println("       Options include:");
         System.err.println("       --laf <LAF>");
         System.err.println("       --cookie <cookie>");
         System.err.println("       --http-header <name> <value>");
         System.exit(-1);
      }
      int i;
      httpHeaders = new HashMap<String, String>();
      for (i = 0; i < args.length-2; i++)
      {
         if (args[i].equals("--laf") && i+1 < args.length-2)
         {
            requestedLaf = args[i+1];
            i++;
         }
         if (args[i].equals("--cookie") && i+1 < args.length-2)
         {
            cookies.add(args[i+1]);
            i++;
         }
         if (args[i].equals("--http-header") && i+2 < args.length-2)
         {
            httpHeaders.put(args[i+1], args[i+2]);
            i+=2;
         }
      }
      httpHeaders.put("Cookie", String.join(";", cookies));
      HTTPContext httpContext = new HTTPContext()
         {
            public Map<String, String> getHTTPHeaders()
            {
               return httpHeaders;
            }
         };
      uri = args[i++];
      component = args[i++];
      SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               try
               {
                  if (requestedLaf != null)
                  {
                     for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
                     {
                        System.out.println(info.getName());
                        if (requestedLaf.equals(info.getName()))
                        {
                           javax.swing.UIManager.setLookAndFeel(info.getClassName());
                           break;
                        }
                     }
                  }
                  currentLaf = javax.swing.UIManager.getLookAndFeel().getName();

                  long startTime = System.currentTimeMillis();

                  new org.proactive.ui.ReactApp(uri, component, httpContext);
                  System.out.println("Render time: " + (System.currentTimeMillis() - startTime ) + "ms");
               }
               catch(Exception e)
               {
                  e.printStackTrace();
                  System.exit(-1);
               }

            }

         });
   }

   private static LinkedList<TreePatch> dispatchQueue = new LinkedList<TreePatch>();   

   public static void queuePatch(Term p, ReactComponent root, Engine engine)
   {
      // FIXME: What ARE we trying to achieve here?
      try
      {
         engine.applyPatch(p, root);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         //System.exit(-1);
      }
/*
      synchronized(dispatchQueue)
      {
         //System.out.println("Received patch: " + p + " for root " + root);
         if (root == null)
            throw new NullPointerException();
         dispatchQueue.offer(new TreePatch(p, root, engine));
      }
      flushQueue();
*/
   }

   private static class TreePatch
   {
      private Term patch;
      private ReactComponent root;
      private Engine engine;
      public TreePatch(Term patch, ReactComponent root, Engine engine)
      {
         this.patch = patch;
         this.root = root;
         this.engine = engine;
      }
      public void apply() throws Exception
      {
         root = engine.applyPatch(patch, root);
      }
   }

   public static void flushQueue()
   {
      SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               flushQueueAsAWT();
            }
         });
   }

   private static void flushQueueAsAWT()
   {
      LinkedList<TreePatch> queue = null;
      synchronized(dispatchQueue)
      {
         // Swap the old queue for a new one
         queue = dispatchQueue;
         dispatchQueue = new LinkedList<TreePatch>();
      }
      TreePatch p;
      while ((p = queue.poll()) != null)
      {
         try
         {
            p.apply();
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   private static StyleSheet styleSheet = new StyleSheet();
   private static List<StyleSheetListener> styleSheetListeners = new LinkedList<StyleSheetListener>();

   public static void addStyleSheetListener(StyleSheetListener l)
   {
      styleSheetListeners.add(l);
   }
   public static void removeStyleSheetListener(StyleSheetListener l)
   {
      styleSheetListeners.remove(l);
   }

   public static void setStyleSheet(StyleSheet styleSheet)
   {
      React.styleSheet = styleSheet;
      for (StyleSheetListener listener: styleSheetListeners)
         listener.styleSheetChanged();
   }

   public static Object getStyle(String id, String className, String type, String key)
   {
      return styleSheet.getValue(id, className, type, key);
   }

}

