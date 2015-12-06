package fr.pludov.ardfocuser.ui;

import fr.pludov.ardfocus.utils.WeakListenerOwner;
import fr.pludov.ardfocuser.driver.Focuser;
import fr.pludov.ardfocuser.driver.FocuserStatus;
import fr.pludov.ardfocuser.driver.IFocuserListener;

public class ConditionPanel extends ConditionPanelDesign {
	private final Focuser focuser;
	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	
	public ConditionPanel(Focuser focuser) {
		this.focuser = focuser;
		this.focuser.listeners.addListener(this.listenerOwner, new IFocuserListener() {
			
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
			
			@Override
			public void filterDefinitionChanged() {
			}
		});
		
		loadLabels();
	}
	
	void loadLabels() {
		if (focuser.getCurrentStatus() == FocuserStatus.Connected) {
			this.paramHeater.setText(focuser.getHeater() == null ? "" : String.format("%.1f %%", focuser.getHeater()));
			this.paramHum.setText(focuser.getExtHum() == null ? "" : String.format("%.1f %%", focuser.getExtHum()));
			this.paramTempExt.setText(focuser.getExtTemp() == null ? "" : String.format("%.2f °", focuser.getExtTemp()));
			this.paramTempScope.setText(focuser.getScopeTemp() == null ? "" : String.format("%.2f °", focuser.getScopeTemp()));
			this.paramVolt.setText(focuser.getBattery() == null ? "" : String.format("%.2f V", focuser.getBattery()));
			
		} else {
			this.paramHeater.setText("N/A");
			this.paramHum.setText("N/A");
			this.paramTempExt.setText("N/A");
			this.paramTempScope.setText("N/A");
			this.paramVolt.setText("N/A");
		}
	}
	

}
