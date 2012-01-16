/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

/**
 *
 * @author manuel
 */
public class DialogAbstract extends JDialog {

	protected static final Dimension defaultPreferredTextfieldDimension = new Dimension(50, (int) new JTextField().getSize().getHeight());
	private final JButton button_abort = new JButton("Abort"),
			button_ok = new JButton("Ok");
	private JButton button_last_clicked = button_abort;

	public DialogAbstract(final Frame owner, final String title, final boolean modal) {
		super(owner, title, modal);
		initDialog();
	}

	public DialogAbstract(final Frame owner, final String title, final ModalityType mtype) {
		super(owner, title, mtype);
		initDialog();
	}

	public void addOptions(Container c) {
		add(c, BorderLayout.CENTER);
	}

	private void initDialog() {
		setLayout(new BorderLayout());

		JPanel bottom = new JPanel(new GridBagLayout());
		add(bottom, BorderLayout.PAGE_END);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		
		bottom.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
				
		JPanel buttons = new JPanel();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		bottom.add(buttons, gbc);
		
		buttons.add(button_abort);
		buttons.add(Box.createHorizontalStrut(5));
		buttons.add(button_ok);
		

		ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(button_ok)) {
					button_last_clicked = button_ok;
				}
				else {
					button_last_clicked = button_abort;
				}
				setVisible(false);
			}
		};
		button_abort.addActionListener(listener);
		button_ok.addActionListener(listener);
		
		getRootPane().setDefaultButton(button_ok);
		
	}

	public boolean aborted() {
		return button_last_clicked.equals(button_abort);
	}

	public boolean closedWithOk() {
		return button_last_clicked.equals(button_ok);
	}
	
	@Override
	public void pack(){
		super.pack();
		setLocationRelativeTo(getOwner());
	}
	
	@Override
	public void setVisible(boolean visible){
		if( visible ){
			pack();
		}
		super.setVisible(visible);
	}
	
}
