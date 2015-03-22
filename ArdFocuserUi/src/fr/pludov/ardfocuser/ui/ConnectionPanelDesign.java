package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import fr.pludov.ardfocuser.ui.util.SteppedComboBox;

public class ConnectionPanelDesign extends JPanel {
	protected JLabel statusLabel;
	protected JComboBox comboBoxPortList;
	protected JButton btnConnect;
	protected JLabel lblPort;
	protected JCheckBox chckbxUsbIdRemind;
	protected JButton btnDisconnect;

	/**
	 * Create the panel.
	 */
	public ConnectionPanelDesign() {
		setLayout(new MigLayout("", "[10px][grow][]", "[10px][][]"));
		
		this.statusLabel = new JLabel("New label");
		add(this.statusLabel, "cell 0 0 3 1,alignx center");
		
		this.lblPort = new JLabel("Port:");
		add(this.lblPort, "cell 0 1,alignx trailing");
		
		this.comboBoxPortList = new SteppedComboBox();
		add(this.comboBoxPortList, "cell 1 1,growx");
		
		this.btnConnect = new JButton("Connecter");
		add(this.btnConnect, "cell 2 1,grow");
		
		this.chckbxUsbIdRemind = new JCheckBox("Retenir l'id usb");
		add(this.chckbxUsbIdRemind, "cell 1 2");
		
		this.btnDisconnect = new JButton("D\u00E9connecter");
		add(this.btnDisconnect, "cell 2 2,grow");

	}
}
