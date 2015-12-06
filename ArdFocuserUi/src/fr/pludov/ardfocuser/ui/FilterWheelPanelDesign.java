package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.SwingConstants;

public class FilterWheelPanelDesign extends JPanel {
	protected JLabel lblStatusLabel;
	protected JComboBox changeFilterComboBox;
	protected JButton btnConfig;

	/**
	 * Create the panel.
	 */
	public FilterWheelPanelDesign() {
		setLayout(new MigLayout("", "[grow][]", "[][]"));
		
		this.lblStatusLabel = new JLabel("Status Label");
		this.lblStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.lblStatusLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
		add(this.lblStatusLabel, "cell 0 0 2 1,grow");
		
		this.changeFilterComboBox = new JComboBox();
		add(this.changeFilterComboBox, "cell 0 1,growx");
		
		this.btnConfig = new JButton("Config");
		add(this.btnConfig, "cell 1 1");

	}

}
