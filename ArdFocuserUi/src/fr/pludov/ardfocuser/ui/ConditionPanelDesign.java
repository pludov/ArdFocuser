package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;

public class ConditionPanelDesign extends JPanel {
	protected JLabel lblTempExtrieure;
	protected JLabel lblHumidit;
	protected JLabel lblTempScope;
	protected JLabel lblBatterie;
	protected JLabel lblChauffage;
	protected JLabel paramTempExt;
	protected JLabel paramHum;
	protected JLabel paramTempScope;
	protected JLabel paramVolt;
	protected JLabel paramHeater;

	/**
	 * Create the panel.
	 */
	public ConditionPanelDesign() {
		setLayout(new MigLayout("", "[right][grow,left]", "[][][][][]"));
		
		this.lblTempExtrieure = new JLabel("Temp ext\u00E9rieure :");
		add(this.lblTempExtrieure, "cell 0 0");
		
		this.paramTempExt = new JLabel("New label");
		add(this.paramTempExt, "cell 1 0");
		
		this.lblHumidit = new JLabel("Humidit\u00E9 :");
		add(this.lblHumidit, "cell 0 1");
		
		this.paramHum = new JLabel("New label");
		add(this.paramHum, "cell 1 1");
		
		this.lblTempScope = new JLabel("Temp scope :");
		add(this.lblTempScope, "cell 0 2");
		
		this.paramTempScope = new JLabel("New label");
		add(this.paramTempScope, "cell 1 2");
		
		this.lblBatterie = new JLabel("Batterie :");
		add(this.lblBatterie, "cell 0 3");
		
		this.paramVolt = new JLabel("New label");
		add(this.paramVolt, "cell 1 3");
		
		this.lblChauffage = new JLabel("Chauffage :");
		add(this.lblChauffage, "cell 0 4");
		
		this.paramHeater = new JLabel("New label");
		add(this.paramHeater, "cell 1 4");

	}

}
