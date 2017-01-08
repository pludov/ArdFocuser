package fr.pludov.ardfocuser.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import fr.pludov.ardfocus.utils.*;
import fr.pludov.ardfocuser.driver.*;
import fr.pludov.ardfocuser.ui.util.*;
import net.miginfocom.swing.*;

public class FilterWheelPanel extends FilterWheelPanelDesign {
	final Focuser focuser;
	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	
	private class CalibrationItem {
		
		@Override
		public String toString() {
			return "Calibration...";
		}
	}
	
	private class GotoItem {
		
		@Override
		public String toString() {
			return "Aller à...";
		}
	}
	
	public FilterWheelPanel(Focuser focuser) {
		this.focuser = focuser;
		
		focuser.listeners.addListener(this.listenerOwner, new IFocuserListener() {

			@Override
			public void statusChanged() {
				loadLabels();
				updateOptions();
			}
			
			@Override
			public void parametersChanged() {
				loadLabels();
				updateOptions();
			}
			
			@Override
			public void filterDefinitionChanged() {
				loadLabels();
				updateOptions();
			}
			
			@Override
			public void broadcastError(String string) {
			}
		});
		loadLabels();
		
		this.btnConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FilterPositionEditorDialog item = DialogUtils.openDialog(FilterWheelPanel.this, FilterPositionEditorDialog.class);
				item.open(FilterWheelPanel.this.focuser);
				item.setVisible(true);
			}
		});
		
		this.changeFilterComboBox.setRenderer(new OptionRenderer());
		this.changeFilterComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				if (changeFilterComboBox.getSelectedIndex() != 0) {
					Object selectedItem = changeFilterComboBox.getSelectedItem();
					if (selectedItem == null) {
						return;
					}
					changeFilterComboBox.setSelectedIndex(0);

					if (selectedItem instanceof FilterDefinition) {
						// demander le changement de filtre
						FocuserRequest request = new FocuserRequest("F" + ((FilterDefinition)selectedItem).getPosition()) {
							
							@Override
							public void onStarted() {
							}
							
							@Override
							public void onReply(String reply) {
								// FIXME: détecter un code d'erreur
							}
							
							@Override
							public void onError(String cause) {
								// FIXME: détecter un code d'erreur
							}
							
							@Override
							public void onCanceled(String cause) {
							}
						};
						FilterWheelPanel.this.focuser.queueRequest(request, false);
					}
					
					if (selectedItem instanceof GotoItem) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								int targetPos = FilterWheelPanel.this.focuser.getFilterWheelPosition();
								
							    String posStr = JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(FilterWheelPanel.this), "Position?", Integer.toString(targetPos));
							    if (posStr != null) {
							    	targetPos = Integer.parseInt(posStr);
							    	// demander la calibration
									FocuserRequest request = new FocuserRequest("F" + targetPos) {
										
										@Override
										public void onStarted() {
										}
										
										@Override
										public void onReply(String reply) {
											// FIXME: détecter un code d'erreur
										}
										
										@Override
										public void onError(String cause) {
											// FIXME: détecter un code d'erreur
										}
										
										@Override
										public void onCanceled(String cause) {
										}
									};
									FilterWheelPanel.this.focuser.queueRequest(request, false);
							    	
							    }
							}
						});

						
					}
					
					if (selectedItem instanceof CalibrationItem) {
						Integer targetPos = null;
						
						for(FilterDefinition fd : FilterWheelPanel.this.focuser.getFilterDefinitions()) {
							if (targetPos == null || targetPos.intValue() > fd.getPosition()) {
								targetPos = fd.getPosition();
							}
						}
						if (targetPos == null) {
							targetPos = 1000;
						}
						// demander la calibration
						FocuserRequest request = new FocuserRequest("Q" + targetPos) {
							
							@Override
							public void onStarted() {
							}
							
							@Override
							public void onReply(String reply) {
								// FIXME: détecter un code d'erreur
							}
							
							@Override
							public void onError(String cause) {
								// FIXME: détecter un code d'erreur
							}
							
							@Override
							public void onCanceled(String cause) {
							}
						};
						FilterWheelPanel.this.focuser.queueRequest(request, false);
						
					}
				}
			}
		});
		updateOptions();
	}

	static class FilterPosition {
		FilterDefinition nearest;
		int distance;
	}

	FilterPosition findNearestFilter(int position)
	{
		if (focuser.getFilterDefinitions().size() == 0) {
			return null;
		}
		FilterPosition result = new FilterPosition();
		for(FilterDefinition fd : focuser.getFilterDefinitions()) {
			int dst = position - fd.getPosition();
			if (result.nearest == null || (Math.abs(dst) < result.distance)) {
				result.nearest = fd;
				result.distance = dst;
			}
		}
		
		return result;
	}
	
	class OptionRenderer extends DefaultListCellRenderer
	{
		@Override
		public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof FilterDefinition) {
				
				JPanel result = new JPanel();
				result.setLayout(new MigLayout("ins 0, wrap 1", "[][grow]", "[]"));
				
		        if (isSelected) {
		            result.setBackground(list.getSelectionBackground());
		            result.setForeground(list.getSelectionForeground());
		        } else {
		        	result.setBackground(list.getBackground());
		        	result.setForeground(list.getForeground());
		        }
				
		        JButton color = new JButton();
		        color.setBackground(((FilterDefinition)value).getColor());
		        result.add(color, "cell 0 0, growy");
		        JLabel label = new JLabel(((FilterDefinition)value).getName());
		        label.setForeground(result.getForeground());
		        result.add(label, "cell 1 0, growx, growy");
				
				return result;
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}
	
	FilterWheelMotorStatus lastOptionStatus;
	List<FilterDefinition> lastDefinitions;
	
	
	void updateOptions()
	{
		// Les options dépendent de l'état et de la liste des filtres.
		FilterWheelMotorStatus newStatus = focuser.getFilterWheelState();
		List<FilterDefinition> newDefinitions = new ArrayList<>(focuser.getFilterDefinitions());
		
		if (Objects.equals(lastOptionStatus, newStatus) 
				&& Objects.equals(lastDefinitions, newDefinitions))
		{
			return;
		}
		lastOptionStatus = newStatus;
		lastDefinitions = newDefinitions;
		
		if (/*focuser.getCurrentStatus() !=  FocuserStatus.Connected ||*/ newStatus == null) {
			this.changeFilterComboBox.setEnabled(false);
			this.changeFilterComboBox.removeAllItems();
		} else {
			this.changeFilterComboBox.setEnabled(true);
			this.changeFilterComboBox.removeAllItems();
			this.changeFilterComboBox.addItem("<Contrôle...>");	
			switch(newStatus) {
			case Moving:
			case Idle:
				for(FilterDefinition fd : focuser.getFilterDefinitions()) {
					this.changeFilterComboBox.addItem(fd);
				}
				this.changeFilterComboBox.addItem(new GotoItem());
			case FailedCalibration:
				this.changeFilterComboBox.addItem(new CalibrationItem());
				break;
			case MovingCalibration:
				break;
			}
		}
	}
	
	void loadLabels()
	{
		FilterWheelMotorStatus filterWheelState = focuser.getFilterWheelState();
		if (focuser.getCurrentStatus() != FocuserStatus.Connected || !focuser.parametersReceived() || filterWheelState == null)
		{
			this.lblStatusLabel.setText("N/A");
			lblStatusLabel.setOpaque(false);
		} else {
			int position = focuser.getFilterWheelPosition();
			Color background = null;
			Color foreground = null;
			String label;
			
			switch(filterWheelState) {
			case FailedCalibration:
				foreground = Color.RED;
				label = "Calibration requise";
				break;
			case MovingCalibration:
				foreground = Color.ORANGE;
				label = "Calibration en cours";
				break;
			case Moving:
			{
				foreground = Color.ORANGE;
				
				FilterPosition p = findNearestFilter(position);
				if (p == null) {
					label = "Déplacement: " + position;
				} else {
					
					label = "Deplacement: " + p.nearest.getName() +  " (" + (p.distance > 0 ? "" : "-") + Math.abs(p.distance) + ")";
					background = p.nearest.getColor();
				}
				break;
			}
			case Idle:
			{
				foreground = Color.BLACK;
				
				FilterPosition p = findNearestFilter(position);
				if (p == null) {
					label = "Pos moteur: " + position;
				} else if (p.distance != 0){
					label = "Pos moteur: " + p.nearest.getName() +  (p.distance > 0 ? "+" : "-") + Math.abs(p.distance);
					foreground = Color.RED;
				} else {
					label = p.nearest.getName();
					background = p.nearest.getColor();
					foreground = ColorContrast.getBestContrastFor(background);
				}
				break;
			}
			default:
				throw new RuntimeException("Still impossible in 2015");
			}
			lblStatusLabel.setOpaque(background != null);
			lblStatusLabel.setBackground(background);
			lblStatusLabel.setForeground(foreground);
			lblStatusLabel.setText(label);
		}
	}
	
}
