package fr.pludov.ardfocuser.ui;

import fr.pludov.ardfocus.utils.WeakListenerOwner;
import fr.pludov.ardfocuser.driver.Focuser;
import fr.pludov.ardfocuser.driver.FocuserStatus;
import fr.pludov.ardfocuser.driver.IFocuserListener;

public class MotorStatusPanel extends MotorStatusPanelDesign {
	Focuser focuser;
	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	
	public MotorStatusPanel(Focuser focuser) {
		this.focuser = focuser;
		focuser.listeners.addListener(this.listenerOwner , new IFocuserListener() {
			
			@Override
			public void statusChanged() {
				loadLabels();
			}
			
			@Override
			public void parametersChanged() {
				loadLabels();
			}
			
			@Override
			public void broadcastError(String string) {
			}
		});
		
		loadLabels();
	}
	
	void loadLabels()
	{
		if (focuser.getCurrentStatus() != FocuserStatus.Connected || !focuser.parametersReceived())
		{
			this.posParameter.setText("N/A");
			this.moveParameter.setText("N/A");
		} else {
			this.posParameter.setText(Integer.toString(focuser.getMotorPosition()));
			this.moveParameter.setText(focuser.getMotorState() ? "Déplacement en cours" : "");
		}
	}
	

}
