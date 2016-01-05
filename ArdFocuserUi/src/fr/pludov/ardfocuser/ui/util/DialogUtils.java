package fr.pludov.ardfocuser.ui.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class DialogUtils {

	private DialogUtils() {
	}
	
	
	public static <DIALOG extends JDialog> DIALOG openDialog(Component c, Class<? extends DIALOG> clazz)
	{
		Window window;
		if (c == null) {
			window = null;
		} else if (c instanceof Window) {
			window = (Window)c;
		} else {
			window = SwingUtilities.getWindowAncestor(c);
		}
		if (window != null) {
			for(Window w : window.getOwnedWindows())
			{
				if (clazz.isInstance(w)) {
					return (DIALOG)w;
				}
			}
		}
		try {
			return clazz.getConstructor(Window.class).newInstance(window);
		} catch(IllegalAccessException e) {
			throw new RuntimeException("Il manque un constructeur avec Window", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Il manque un constructeur avec Window", e);
		} catch (SecurityException e) {
			throw new RuntimeException("Il manque un constructeur avec Window", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Il manque un constructeur avec Window", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Il manque un constructeur avec Window", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Il manque un constructeur avec Window", e);
		}
	}

	public static interface DialogValidator {
		boolean close();
	}
	
	public static void buildOkCancel(final JDialog dialog, final DialogValidator onClose)
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("OK");
		ok.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (onClose.close()) {
					closeDialog(dialog);
				}
			}
		});
		dialog.getRootPane().setDefaultButton( ok );
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				closeDialog(dialog);
			}
		});
		
		buttonPanel.add(ok);
		buttonPanel.add(cancel);
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		addCancelByEscapeKey(dialog);
	}
	
	/**
	  * Force the escape key to call the same action as pressing the Cancel button.
	  *
	  * <P>This does not always work. See class comment.
	  */
	private static void addCancelByEscapeKey(final JDialog fDialog){
	    String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
	    int noModifiers = 0;
	    KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
	    InputMap inputMap = 
	      fDialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
	    ;
	    inputMap.put(escapeKey, CANCEL_ACTION_KEY);
	    AbstractAction cancelAction = new AbstractAction(){
	      @Override public void actionPerformed(ActionEvent e){
	        closeDialog(fDialog);
	      }
	    }; 
	    fDialog.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
	  }
	
	private static void closeDialog(JDialog fDialog) {
		fDialog.dispose();
	}
	
}
