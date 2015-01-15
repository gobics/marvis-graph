package de.gobics.marvis.utils.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;

/**
 *
 * @author manuel
 */
public class ActionFire {
	private final Component parent;
	private final TreeSet<ActionListener> listener = new TreeSet<ActionListener>();

	public ActionFire(Component parent) {
		this.parent = parent;
	}
	
	public void addActionListener(ActionListener l){
		listener.add(l);
	}
	
	public void removeActionListener(ActionListener l){
		listener.remove(l);
	}
	
	public void fireActionEvent(){
		fireActionEvent(parent.getName());
	}
	public void fireActionEvent(String command){
		ActionEvent e = new ActionEvent(parent, ActionEvent.ACTION_FIRST, command);
		for(ActionListener l : listener){
			l.actionPerformed(e);
		}
	}
		
}
