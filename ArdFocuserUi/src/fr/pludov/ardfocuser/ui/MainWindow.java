package fr.pludov.ardfocuser.ui;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
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
	FilterWheelPanel filterWheelPanel;
	ConditionPanel conditionPanel;
	Focuser focuser;
	
	MainPanelDesign mpd;
	
	public MainWindow() throws HeadlessException {
		setIconImages(loadImages("db.png", "db32.png", "db24.png", "db22.png", "db16.png"));
		this.focuser = new Focuser();
		setTitle("Arduino Focuser Control");
		setLayout(new BorderLayout());
		
		mpd = new MainPanelDesign();
		add(mpd, BorderLayout.CENTER);
		
		connectionPanel = new ConnectionPanel(focuser);
		mpd.connectionPanel.add(connectionPanel);
		
		motorStatusPanel = new MotorStatusPanel(focuser);
		mpd.focuseurPanel.add(motorStatusPanel);
		
		filterWheelPanel = new FilterWheelPanel(focuser);
		mpd.filterWheelPanel.add(filterWheelPanel);
		
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

	
	private static List<? extends Image> loadImages(String ... images)
	{
		List<Image> result = new ArrayList<>();
		for(String fileName : images) {
			String imagePath = "/" + MainWindow.class.getPackage().getName().replaceAll("\\.", "/") + "/" +  fileName;
			ImageIcon imageicon = new ImageIcon(MainWindow.class.getResource(imagePath));
			result.add(imageicon.getImage());
		}
		return result;
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
