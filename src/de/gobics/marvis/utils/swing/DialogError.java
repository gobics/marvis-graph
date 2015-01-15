package de.gobics.marvis.utils.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;
public class DialogError extends DialogCentered implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel errPanel = new JPanel();

	public DialogError(String errstr) {
		super();
		this.init(errstr);
	}

	public DialogError(Frame owner, String errstr) {
		super(owner);
		this.init(errstr);
	}

	public DialogError(Dialog owner, String errstr) {
		super(owner);
		this.init(errstr);
	}

	public DialogError(Window owner, String errstr) {
		super(owner);
		this.init(errstr);
	}

	public DialogError(Frame owner, boolean modal, String errstr) {
		super(owner, modal);
		this.init(errstr);
	}

	public DialogError(Frame owner, String title, String errstr) {
		super(owner, title);
		this.init(errstr);
	}

	public DialogError(Dialog owner, boolean modal, String errstr) {
		super(owner, modal);
		this.init(errstr);
	}

	public DialogError(Dialog owner, String title, String errstr) {
		super(owner, title);
		this.init(errstr);
	}

	public DialogError(Window owner, ModalityType modalityType, String errstr) {
		super(owner, modalityType);
		this.init(errstr);
	}

	public DialogError(Window owner, String title, String errstr) {
		super(owner, title);
		this.init(errstr);
	}

	public DialogError(Frame owner, String title, boolean modal, String errstr) {
		super(owner, title, modal);
		this.init(errstr);
	}

	public DialogError(Dialog owner, String title, boolean modal, String errstr) {
		super(owner, title, modal);
		this.init(errstr);
	}

	public DialogError(Window owner, String title, ModalityType modalityType, String errstr) {
		super(owner, title, modalityType);
		this.init(errstr);
	}

	public DialogError(Frame owner, String title, boolean modal, GraphicsConfiguration gc, String errstr) {
		super(owner, title, modal, gc);
		this.init(errstr);
	}

	public DialogError(Dialog owner, String title, boolean modal, GraphicsConfiguration gc, String errstr) {
		super(owner, title, modal, gc);
		this.init(errstr);
	}

	public DialogError(Window owner, String title, ModalityType modalityType,
			GraphicsConfiguration gc, String errstr) {
		super(owner, title, modalityType, gc);
		this.init(errstr);
	}

	public DialogError(Window parent, String errstr, Exception e) {
		super(parent);
		init(errstr);
		setException(e);
	}

	private void init(String errstr) {
		if (getParent() == null) {
			this.setModalityType(ModalityType.APPLICATION_MODAL);
		} else {
			setModalityType(ModalityType.DOCUMENT_MODAL);
		}
		this.setTitle("Error");
		this.setMinimumSize(new Dimension(200, 100));

		JPanel main = new JPanel(new BorderLayout());
		setContentPane(main);
		
		errPanel.setLayout(new BoxLayout(errPanel, BoxLayout.PAGE_AXIS));
		errPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		main.add(errPanel, BorderLayout.CENTER);
		errPanel.add(new JLabel(errstr));
		

		JPanel button_panel = new JPanel();
		button_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.LINE_AXIS));
		main.add(button_panel, BorderLayout.PAGE_END);

		JButton ok = new JButton("Ok");
		ok.setMaximumSize(new Dimension(100, 50));
		ok.addActionListener(this);

		// Add ok button
		button_panel.add(Box.createHorizontalGlue());
		button_panel.add(ok);
		button_panel.add(Box.createHorizontalGlue());

		pack();
	}

	public void setException(Exception exc) {
		final StringWriter result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		exc.printStackTrace(printWriter);

		errPanel.add(Box.createVerticalStrut(5));

		for(String line : result.toString().split("\n")){
			errPanel.add(new JLabel(line));
		}
		pack();
	}

	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}
}
