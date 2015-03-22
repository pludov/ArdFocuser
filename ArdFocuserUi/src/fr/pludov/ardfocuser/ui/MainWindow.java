package fr.pludov.ardfocuser.ui;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import fr.pludov.ardfocuser.driver.Focuser;
import fr.pludov.ardfocuser.driver.FocuserStatus;

public class MainWindow extends JFrame {

	StatusBar statusBar;
	ConnectionPanel connectionPanel;
	MotorStatusPanel motorStatusPanel;
	ConditionPanel conditionPanel;
	Focuser focuser;
	
	MainPanelDesign mpd;
	
	public MainWindow() throws HeadlessException {
		this.focuser = new Focuser();
		setTitle("Arduino Focuser Control");
		setLayout(new BorderLayout());
		
		mpd = new MainPanelDesign();
		add(mpd, BorderLayout.CENTER);
		
		connectionPanel = new ConnectionPanel(focuser);
		mpd.connectionPanel.add(connectionPanel);
		
		motorStatusPanel = new MotorStatusPanel(focuser);
		mpd.focuseurPanel.add(motorStatusPanel);
		
		conditionPanel = new ConditionPanel(focuser);
		mpd.conditionPanel.add(conditionPanel);
		
		statusBar = new StatusBar(focuser);
		add(statusBar, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {}
			
			@Override
			public void windowIconified(WindowEvent arg0) {}
			
			@Override
			public void windowDeiconified(WindowEvent arg0) {}
			
			@Override
			public void windowDeactivated(WindowEvent arg0) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				if (focuser.getCurrentStatus() != FocuserStatus.Disconnected) {
					int rslt = JOptionPane.showConfirmDialog(MainWindow.this, "Confirmer la fermeture du driver Arduino ?", "Attention!", JOptionPane.YES_NO_OPTION);
					if (rslt != JOptionPane.YES_OPTION) {
						return;
					}
				}
				MainWindow.this.dispose();
				System.exit(0);
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {}
			
			@Override
			public void windowActivated(WindowEvent arg0) {}
		});
		
		pack();
	}

	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch(Throwable t) {
					t.printStackTrace();
				}
				MainWindow window = new MainWindow();
				window.setVisible(true);
			}
		});
	}
}
