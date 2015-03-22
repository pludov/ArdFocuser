package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;

public class MotorStatusPanelDesign extends JPanel {
	protected JLabel lblPosition;
	protected JLabel posParameter;
	protected JLabel moveParameter;

	/**
	 * Create the panel.
	 */
	public MotorStatusPanelDesign() {
		setLayout(new MigLayout("", "[][grow,fill]", "[][]"));
		
		this.lblPosition = new JLabel("Position :");
		add(this.lblPosition, "cell 0 0");
		
		this.posParameter = new JLabel("New label");
		add(this.posParameter, "cell 1 0");
		
		this.moveParameter = new JLabel("moveLabel");
		add(this.moveParameter, "cell 1 1");

	}

}
