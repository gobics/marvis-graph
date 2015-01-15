package de.gobics.marvis.utils.swing;

import java.awt.*;

import javax.swing.JDialog;

public class DialogCentered extends JDialog {
	private static final long serialVersionUID = 1L;

	public DialogCentered() {
		this.doCentering();
	}

	public DialogCentered(Frame owner) {
		super(owner);
		this.doCentering();
	}

	public DialogCentered(Dialog owner) {
		super(owner);
		this.doCentering();
	}

	public DialogCentered(Window owner) {
		super(owner);
		this.doCentering();
	}

	public DialogCentered(Frame owner, boolean modal) {
		super(owner, modal);
		this.doCentering();
	}

	public DialogCentered(Frame owner, String title) {
		super(owner, title);
		this.doCentering();
	}

	public DialogCentered(Dialog owner, boolean modal) {
		super(owner, modal);
		this.doCentering();
	}

	public DialogCentered(Dialog owner, String title) {
		super(owner, title);
		this.doCentering();
	}

	public DialogCentered(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		this.doCentering();
	}

	public DialogCentered(Window owner, String title) {
		super(owner, title);
		this.doCentering();
	}

	public DialogCentered(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		this.doCentering();
	}

	public DialogCentered(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public DialogCentered(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		this.doCentering();
	}

	public DialogCentered(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		this.doCentering();
	}

	public DialogCentered(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		this.doCentering();
	}

	public DialogCentered(Window owner, String title, ModalityType modalityType,
			GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		this.doCentering();
	}

	public void doCentering(){
		this.setLocationRelativeTo(null);
	}
	
	@Override
	public void pack(){
		super.pack();
		this.setMinimumSize(this.getSize());
		this.doCentering();
	}
	
	
}
