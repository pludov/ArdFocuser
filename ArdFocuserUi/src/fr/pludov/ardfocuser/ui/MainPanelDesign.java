package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import javax.swing.JLabel;

public class MainPanelDesign extends JPanel {
	protected JPanel connectionPanel;
	protected JPanel focuseurPanel;
	protected JPanel conditionPanel;

	/**
	 * Create the panel.
	 */
	public MainPanelDesign() {
		setLayout(new MigLayout("", "[grow]", "[][][]"));
		
		this.connectionPanel = new JPanel();
		add(this.connectionPanel, "cell 0 0,grow");
		this.connectionPanel.setLayout(new BorderLayout(0, 0));
		
		this.focuseurPanel = new JPanel();
		this.focuseurPanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Focuseur", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(this.focuseurPanel, "cell 0 1,grow");
		this.focuseurPanel.setLayout(new BorderLayout(0, 0));
		
		this.conditionPanel = new JPanel();
		this.conditionPanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Conditions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(this.conditionPanel, "cell 0 2,grow");
		this.conditionPanel.setLayout(new BorderLayout(0, 0));

	}

}
