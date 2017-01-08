package fr.pludov.ardfocuser.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import fr.pludov.ardfocus.utils.*;
import fr.pludov.ardfocuser.driver.*;

public class MotorStatusPanel extends MotorStatusPanelDesign {
	Focuser focuser;
	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	MotorControlPanel controlPanel;
	Color defaultForeground;
	
	public MotorStatusPanel(Focuser focuser) {
		this.controlPanel = new MotorControlPanel();
		this.defaultForeground = this.controlPanel.btnStop.getForeground();
		this.buttonPanel.add(this.controlPanel);
		configureMoveButton(this.controlPanel.btonFastBackward, -5);
		configureMoveButton(this.controlPanel.btonBackward, -1);
		configureMoveButton(this.controlPanel.btonForward, 1);
		configureMoveButton(this.controlPanel.btonFastForward, 5);
		configureStopButton(this.controlPanel.btnStop);
		
		
		
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
			public void filterDefinitionChanged() {
			}
			
			@Override
			public void broadcastError(String string) {
			}
		});
		
		loadLabels();
	}
	
	private void configureMoveButton(JButton button, int amount)
	{
		final int moveUnit = 50 * amount;

		button.setToolTipText((amount > 0 ? "Avance " : "Recule ") + " de " + moveUnit  + " pas");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (focuser.getCurrentStatus() != FocuserStatus.Connected) {
					return;
				}
				int currentPos = focuser.getMotorPosition();
				int targetPos = currentPos + 4 * moveUnit;
				if (targetPos < 0) {
					targetPos = 0;
				}
				if (targetPos > focuser.getMaxMotorPosition()) {
					targetPos = focuser.getMaxMotorPosition();
				}
				if (targetPos == currentPos) {
					return;
				}
				FocuserRequest request = new FocuserRequest("T" + targetPos) {
					
					@Override
					public void onStarted() {
					}
					
					@Override
					public void onReply(String reply) {
					}
					
					@Override
					public void onError(String cause) {
					}
					
					@Override
					public void onCanceled(String cause) {
					}
				};
				focuser.queueRequest(request, false);
			}
		});
	}

	private void configureStopButton(JButton button)
	{
		button.setToolTipText("Arrete le moteur");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (focuser.getCurrentStatus() != FocuserStatus.Connected) {
					return;
				}
				FocuserRequest request = new FocuserRequest("H") {
					
					@Override
					public void onStarted() {
					}
					
					@Override
					public void onReply(String reply) {
					}
					
					@Override
					public void onError(String cause) {
					}
					
					@Override
					public void onCanceled(String cause) {
					}
				};
				focuser.queueRequest(request, false);
			}
		});
	}

	void loadLabels()
	{
		if (focuser.getCurrentStatus() != FocuserStatus.Connected || !focuser.parametersReceived())
		{
			this.posParameter.setText("N/A");
			this.moveParameter.setText("N/A");
			this.controlPanel.btnStop.setEnabled(false);
			this.controlPanel.btnStop.setForeground(this.defaultForeground);
			this.controlPanel.btonBackward.setEnabled(false);
			this.controlPanel.btonForward.setEnabled(false);
			this.controlPanel.btonFastBackward.setEnabled(false);
			this.controlPanel.btonFastForward.setEnabled(false);
		} else {
			this.posParameter.setText(Integer.toString(focuser.getMotorPosition()));
			this.moveParameter.setText(focuser.getMotorState() ? "Déplacement en cours" : " ");
			
			int mpos = focuser.getMotorPosition();
			int maxPos = focuser.getMaxMotorPosition();
			this.controlPanel.btnStop.setEnabled(focuser.getMotorState());
			this.controlPanel.btnStop.setForeground(focuser.getMotorState() ? Color.RED : this.defaultForeground);
			this.controlPanel.btonBackward.setEnabled(!focuser.getMotorState() && mpos > 0);
			this.controlPanel.btonForward.setEnabled(!focuser.getMotorState() && mpos > 0);
			this.controlPanel.btonFastBackward.setEnabled(!focuser.getMotorState() && mpos < maxPos);
			this.controlPanel.btonFastForward.setEnabled(!focuser.getMotorState() && mpos < maxPos);
		}
	}
	

}
