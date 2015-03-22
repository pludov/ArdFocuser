package fr.pludov.ardfocuser.ui;

import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import fr.pludov.ardfocus.utils.WeakListenerOwner;
import fr.pludov.ardfocuser.driver.Focuser;
import fr.pludov.ardfocuser.driver.IFocuserListener;

public class StatusBar extends JPanel {

	final Focuser focuser;

	final JLabel statusLabel;

	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	
	public StatusBar(Focuser focuser) {
		this.focuser = focuser;
		
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		setPreferredSize(new Dimension(0, 16));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		statusLabel = new JLabel("status");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		add(statusLabel);
		
		statusLabel.setText("");
		
		focuser.listeners.addListener(this.listenerOwner, new IFocuserListener() {
			
			@Override
			public void statusChanged() {
			}
			
			@Override
			public void parametersChanged() {
			}
			
			@Override
			public void broadcastError(String string) {
				statusLabel.setText(string);
			}
		});
	}

}
