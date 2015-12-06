package fr.pludov.ardfocuser.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jssc.SerialPort;
import fr.pludov.ardfocus.utils.WeakListenerOwner;
import fr.pludov.ardfocuser.driver.Focuser;
import fr.pludov.ardfocuser.driver.FocuserStatus;
import fr.pludov.ardfocuser.driver.IFocuserListener;

public class ConnectionPanel extends ConnectionPanelDesign {
	
	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	private final Focuser focuser;
	
	public ConnectionPanel(Focuser focuser) {
		this.focuser = focuser;
		this.comboBoxPortList.setPrototypeDisplayValue("COM999");
		focuser.listeners.addListener(this.listenerOwner, new IFocuserListener() {
			@Override
			public void statusChanged() {
				ConnectionPanel.this.statusChanged();
			}
			
			@Override
			public void broadcastError(String string) {
			}
			
			@Override
			public void parametersChanged() {
			}
			
			@Override
			public void filterDefinitionChanged() {
			}
		});
		
		this.btnConnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ConnectionPanel.this.connect();
			}
		});
		
		this.btnDisconnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (ConnectionPanel.this.focuser.getCurrentStatus() == FocuserStatus.Connected) {
					int rslt = JOptionPane.showConfirmDialog(ConnectionPanel.this, "Confirmer la déconnection de l'Arduino ?", "Attention!", JOptionPane.YES_NO_OPTION);
					if (rslt != JOptionPane.YES_OPTION) {
						return;
					}
				}
				ConnectionPanel.this.focuser.close();
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scanPorts();	
			}
		});
		
		statusChanged();
	}
	
	private class CommPortLabel
	{
		String title;
		String name;
		
		public CommPortLabel(String title, String name) {
			this.title = title;
			this.name = name;
		}

		@Override
		public String toString() {
			return title;
		}
	}
	
	void connect()
	{
		FocuserStatus fs = focuser.getCurrentStatus();
		if (fs != FocuserStatus.Disconnected) return;
		
		String id;
		Object selectedItem = this.comboBoxPortList.getSelectedItem();
		if (selectedItem instanceof String) {
			id = (String)selectedItem;
		} else if (selectedItem instanceof CommPortLabel) {
			id = ((CommPortLabel)selectedItem).name;
		} else {
			focuser.listeners.getTarget().broadcastError("Port invalide");
			return;
		}

		SerialPort serialPort = new SerialPort(id);
		
		focuser.use(serialPort);
	}
	
	void diconnect()
	{
		focuser.close();
	}
	
	void statusChanged()
	{
		FocuserStatus fs = focuser.getCurrentStatus();
		switch(fs) {
		case Disconnected:
			this.btnConnect.setEnabled(true);
			this.btnDisconnect.setEnabled(false);
			this.comboBoxPortList.setEnabled(true);
			this.statusLabel.setText("Pas connecté");
			this.statusLabel.setForeground(Color.red);
			break;
		case InitialHandshake:
			this.btnConnect.setEnabled(false);
			this.btnDisconnect.setEnabled(true);
			this.comboBoxPortList.setEnabled(false);
			this.statusLabel.setText("Connection en cours...");
			this.statusLabel.setForeground(Color.orange);
			break;
		case Connected:
			this.btnConnect.setEnabled(false);
			this.btnDisconnect.setEnabled(true);
			this.comboBoxPortList.setEnabled(false);
			this.statusLabel.setText("Connecté");
			this.statusLabel.setForeground(Color.green);
			break;
		}
	}

	void scanPorts()
	{
//		
//		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
//
//		while (portList.hasMoreElements()) {
//			CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
//			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
//				comboBoxPortList.addItem(portId.getName());
//			}
//		}
		ProcessBuilder pb = new ProcessBuilder("C:\\Documents and Settings\\utilisateur\\git\\ArdFocuser\\ArdFocuserUi\\jars\\listComPorts.exe");
		Redirect output = pb.redirectOutput();
		
		Process p;
		try {
			p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			Pattern regexp = Pattern.compile("^(COM\\d+).*");
			while ( (line = reader.readLine()) != null) {
				
				Matcher m = regexp.matcher(line);
				if (m.matches()) {
					String name = m.group(1);
					CommPortLabel clabel = new CommPortLabel(line, name);
					comboBoxPortList.addItem(clabel);
				}
	   		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
