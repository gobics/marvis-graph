package de.gobics.marvis.utils.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * A popup menu that can be added to a {@link JTextComponent}.
 *
 * @author manuel
 */
public class TextPopupMenu extends JPopupMenu {

	private static final Logger logger = Logger.getLogger(TextPopupMenu.class.getName());
	private final Document document;
	private final JTextComponent component;

	private TextPopupMenu(JTextComponent tc) {
		this.document = tc.getDocument();
		this.component = tc;


		add(new JMenuItem(new CopyAction()));
		add(new JMenuItem(new CutAction()));
		add(new JMenuItem(new PasteAction()));

		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				popup(me);
			}

			@Override
			public void mouseClicked(MouseEvent me) {
				popup(me);
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				popup(me);
			}
		});
	}

	/**
	 * Creates and adds a {@link TextPopupMenu} to the given
	 * {@link JTextComponent}.
	 *
	 * @param field
	 */
	public static void create(JTextComponent field) {
		new TextPopupMenu(field);
	}

	/**
	 * Check if this is a popup request and if yes, display it.
	 *
	 * @param me
	 */
	public void popup(MouseEvent me) {
		if (me.isPopupTrigger()) {
			show(me.getComponent(), me.getX(), me.getY());
		}
	}

	/**
	 * An actions working on the text.
	 */
	private abstract class TextAction extends AbstractAction {

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		public TextAction(String name, Icon icon) {
			super(name, icon);
			if (clipboard == null) {
				setEnabled(false);
			}
		}

		protected void toClipBoard(String text) {
			if (clipboard != null) {
				clipboard.setContents(new StringSelection(text), null);
			}
		}

		protected String fromClipBoard() {
			if (clipboard != null) {
				Transferable tf = clipboard.getContents(null);
				try {
					return (String) tf.getTransferData(DataFlavor.stringFlavor);
				}
				catch (UnsupportedFlavorException ex) {
					logger.log(Level.SEVERE, null, ex);
				}
				catch (IOException ex) {
					logger.log(Level.SEVERE, null, ex);
				}

			}
			return null;
		}
	}

	private class CopyAction extends TextAction {

		public CopyAction() {
			super("Copy", SilkIcon.getCopy());
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			Caret c = component.getCaret();
			if (c.getDot() != c.getMark()) {
				int start = c.getDot() < c.getMark() ? c.getDot() : c.getMark();
				int end = c.getDot() > c.getMark() ? c.getDot() : c.getMark();
				try {
					toClipBoard(document.getText(start, end - start));
				}
				catch (BadLocationException ex) {
					logger.log(Level.SEVERE, "getText({0}, {1}) failed: {2}", new Object[]{start, end, ex});
				}
			}
		}
	}

	private class PasteAction extends TextAction {

		public PasteAction() {
			super("Paste", SilkIcon.getPaste());
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			String clip = fromClipBoard();
			if (clip == null) {
				logger.warning("No content in clipboard");
				return;
			}

			// Get the caret
			Caret c = component.getCaret();
			if (c.getDot() == c.getMark()) {
				try {
					document.insertString(c.getDot(), clip, null);
				}
				catch (BadLocationException ex) {
					logger.log(Level.SEVERE, null, ex);
				}
			}
			else {
				try {
					int start = c.getDot() < c.getMark() ? c.getDot() : c.getMark();
					int end = c.getDot() > c.getMark() ? c.getDot() : c.getMark();
					document.remove(start, end - start);
					document.insertString(start, clip, null);
				}
				catch (BadLocationException ex) {
					logger.log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private class CutAction extends TextAction {

		public CutAction() {
			super("Cut", SilkIcon.getCut());
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			Caret c = component.getCaret();
			if (c.getDot() != c.getMark()) {
				int start = c.getDot() < c.getMark() ? c.getDot() : c.getMark();
				int end = c.getDot() > c.getMark() ? c.getDot() : c.getMark();
				try {
					toClipBoard(document.getText(start, end - start));
					document.remove(start, end - start);
				}
				catch (BadLocationException ex) {
					logger.log(Level.SEVERE, "Can not cut from {0} to {1} (length of {2}): {3}", new Object[]{start, end, end - start, ex});
				}
			}
		}
	}
}
