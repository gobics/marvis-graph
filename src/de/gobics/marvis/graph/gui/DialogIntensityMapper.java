package de.gobics.marvis.graph.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import de.gobics.marvis.graph.*;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

public class DialogIntensityMapper extends JPanel {

	private static final Logger logger = Logger.getLogger(DialogIntensityMapper.class.
			getName());
	private final JLabel[] orignalName;
	private final JComboBox[] use_known_condition;
	private final JTextField[] use_new_condition;

	public DialogIntensityMapper(MetabolicNetwork network, String[] header_names) {
		orignalName = new JLabel[header_names.length];
		use_known_condition = new JComboBox[header_names.length];
		use_new_condition = new JTextField[header_names.length];

		String[] sorted_condition_names = network.getConditionNames();
		Arrays.sort(sorted_condition_names);
		String[] condition_names = new String[sorted_condition_names.length + 1];
		condition_names[0] = null;
		for (int i = 0; i < sorted_condition_names.length; i++) {
			condition_names[i + 1] = sorted_condition_names[i];
		}

		JPanel options = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		Dimension textfield_dimension = new JTextField().getPreferredSize();
		textfield_dimension.width = 250;

		for (int i = 0; i < header_names.length; i++) {
			gbc.gridy = i;
			gbc.gridx = 0;

			orignalName[i] = new JLabel(header_names[i]);
			options.add(new JLabel(Integer.toString(i + 1)), gbc);
			gbc.gridx++;
			options.add(Box.createHorizontalStrut(5), gbc);

			gbc.gridx++;
			options.add(orignalName[i], gbc);
			header_names[i] = header_names[i].toLowerCase();
			gbc.gridx++;
			options.add(Box.createHorizontalStrut(5), gbc);


			use_known_condition[i] = new JComboBox(condition_names);
			use_known_condition[i].setSelectedItem(header_names[i]);
			gbc.gridx++;
			options.add(use_known_condition[i], gbc);
			gbc.gridx++;
			options.add(Box.createHorizontalStrut(5), gbc);

			use_new_condition[i] = new JTextField();
			// Try to identify the condition
			header_names[i].replaceAll("uvw", "0min");
			Matcher matcher = Pattern.compile("[^_]+_\\d+((min)|h)").matcher(header_names[i]);
			if (matcher.find()) {
				use_new_condition[i] = new JTextField(header_names[i].substring(matcher.
						start(),
						matcher.end()));
			}
			else {
				use_new_condition[i] = new JTextField(header_names[i]);
			}
			use_new_condition[i].setSize(textfield_dimension);
			gbc.gridx++;
			options.add(use_new_condition[i], gbc);


			// Button to use above value
			gbc.gridx++;
			if (i > 0) {
				JButton b = new ButtonLastConditionName(use_known_condition[i - 1], use_known_condition[i], use_new_condition[i - 1], use_new_condition[i]);
				options.add(b, gbc);
			}
			JButton b = new ButtonDistributeConditionName(i, use_known_condition, use_new_condition);
			gbc.gridx++;
			options.add(b, gbc);
		}
		
		
		JScrollPane spane = new JScrollPane(options);
		spane.setPreferredSize(new Dimension(600, 400));
		add(spane);
	}

	public String[] getConditionMapping() {
		String[] conditionNames = new String[orignalName.length];
		for (int i = 0; i < orignalName.length; i++) {
			if (use_known_condition[i].getSelectedItem() != null) {
				conditionNames[i] = (String) use_known_condition[i].
						getSelectedItem();
			}
			else if (use_new_condition[i].getText() != null
					&& !use_new_condition[i].getText().isEmpty()) {
				conditionNames[i] = use_new_condition[i].getText();
			}
		}
		return conditionNames;
	}

	public boolean showDialog(MarvisGraphMainWindow parent) {
		return JOptionPane.showConfirmDialog(parent, this, "Map condition names", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
	}
}

class ButtonLastConditionName extends JButton {

	private final JTextField last_field;
	private final JComboBox last_box;
	private final JComboBox next_box;
	private final JTextField next_field;

	public ButtonLastConditionName(final JComboBox last_box, final JComboBox next_box, final JTextField last_field, final JTextField next_field) {
		super("<");
		setToolTipText("Use name from field above");

		this.last_box = last_box;
		this.next_box = next_box;
		this.last_field = last_field;
		this.next_field = next_field;

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (last_box.getSelectedIndex() >= 0) {
					next_box.setSelectedIndex(last_box.getSelectedIndex());
				}
				else {
					next_field.setText(last_field.getText());
				}
			}
		});

	}
}

class ButtonDistributeConditionName extends JButton {

	private final int index;
	private final JComboBox[] boxes;
	private final JTextField[] fields;

	public ButtonDistributeConditionName(int this_index, final JComboBox[] boxes, final JTextField[] fields) {
		super(">>");
		setToolTipText("Use this condition name for all following conditions");

		this.index = this_index;
		this.boxes = boxes;
		this.fields = fields;

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selection_index = boxes[index].getSelectedIndex();
				if (selection_index >= 0) {
					for (int idx = index + 1; idx < boxes.length; idx++) {
						boxes[idx].setSelectedIndex(selection_index);
					}
				}
				else {
					String text = fields[index].getText();
					for (int idx = index + 1; idx < boxes.length; idx++) {
						fields[idx].setText(text);
					}
				}
			}
		});

	}
}