package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FlowLayout;

public class MotorStatusPanelDesign extends JPanel {
	protected JLabel lblPosition;
	protected JLabel posParameter;
	protected JLabel moveParameter;
	protected JPanel buttonPanel;

	/**
	 * Create the panel.
	 */
	public MotorStatusPanelDesign() {
		setLayout(new MigLayout("", "[][grow,fill]", "[][][]"));
		
		this.lblPosition = new JLabel("Position :");
		add(this.lblPosition, "cell 0 0");
		
		this.posParameter = new JLabel("New label");
		this.posParameter.setFont(new Font("Tahoma", Font.BOLD, 13));
		add(this.posParameter, "cell 1 0");
		
		this.moveParameter = new JLabel("moveLabel");
		add(this.moveParameter, "cell 1 1");
		
		this.buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) this.buttonPanel.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		add(this.buttonPanel, "cell 0 2 2 1,grow");

	}

}
