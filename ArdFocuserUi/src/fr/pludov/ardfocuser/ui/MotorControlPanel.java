package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JButton;

public class MotorControlPanel extends JPanel {
	JButton btonFastBackward;
	JButton btonBackward;
	JButton btonForward;
	JButton btonFastForward;
	JButton btnStop;

	/**
	 * Create the panel.
	 */
	public MotorControlPanel() {
		setLayout(new MigLayout("", "[grow,right][][][][grow,left]", "[]"));
		
		this.btonFastBackward = new JButton("<<");
		add(this.btonFastBackward, "cell 0 0");
		
		this.btonBackward = new JButton("<");
		add(this.btonBackward, "cell 1 0");
		
		this.btnStop = new JButton("STOP");
		add(this.btnStop, "cell 2 0");
		
		this.btonForward = new JButton(">");
		add(this.btonForward, "cell 3 0");
		
		this.btonFastForward = new JButton(">>");
		add(this.btonFastForward, "cell 4 0");

	}

}
