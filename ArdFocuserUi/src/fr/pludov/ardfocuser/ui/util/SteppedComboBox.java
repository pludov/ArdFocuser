package fr.pludov.ardfocuser.ui.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

class StyledComboBoxUI extends BasicComboBoxUI {
	protected ComboPopup createPopup() {
		BasicComboPopup popup = new BasicComboPopup(comboBox) {
			@Override
			protected Rectangle computePopupBounds(int px,int py,int pw,int ph) {
				return super.computePopupBounds(
						px,py,Math.max(comboBox.getPreferredSize().width + 250,pw),ph
						);
			}
		};
		popup.getAccessibleContext().setAccessibleParent(comboBox);
		return popup;
	}
}

public class SteppedComboBox extends JComboBox {
	public SteppedComboBox() {
		setUI(new StyledComboBoxUI());
	}
	
	private boolean layingOut = false; 

    public void doLayout(){ 
        try{ 
            layingOut = true; 
                super.doLayout(); 
        }finally{ 
            layingOut = false; 
        } 
    } 

    public Dimension getSize(){ 
        Dimension dim = super.getSize(); 
        if(!layingOut) 
            dim.width = Math.max(dim.width, getPreferredSize().width); 
        return dim; 
    } 
}
//public class SteppedComboBox extends JComboBox {
//	protected int popupWidth;
//
//	public SteppedComboBox(ComboBoxModel aModel) {
//		super(aModel);
//		setUI(new SteppedComboBoxUI());
//		popupWidth = 0;
//	}
//
//	public SteppedComboBox() {
//		super();
//		setUI(new SteppedComboBoxUI());
//		popupWidth = 0;
//	}
//
//	public SteppedComboBox(final Object[] items) {
//		super(items);
//		setUI(new SteppedComboBoxUI());
//		popupWidth = 0;
//	}
//
//	public SteppedComboBox(Vector items) {
//		super(items);
//		setUI(new SteppedComboBoxUI());
//		popupWidth = 0;
//	}
//
//	public void setPopupWidth(int width) {
//		popupWidth = width;
//	}
//
//	public Dimension getPopupSize() {
//		Dimension size = getSize();
//		size = super.getPreferredSize();
//		if (popupWidth < 1)
//			popupWidth = size.width;
//		return new Dimension(popupWidth, size.height);
//	}
//
//	static class SteppedComboBoxUI extends MetalComboBoxUI {
//		protected ComboPopup createPopup() {
//			BasicComboPopup popup = new BasicComboPopup(comboBox) {
//
//				public void show() {
//					Dimension popupSize = ((SteppedComboBox) comboBox)
//							.getPopupSize();
//					popupSize
//					.setSize(popupSize.width,
//							getPopupHeightForRowCount(comboBox
//									.getMaximumRowCount()));
//					Rectangle popupBounds = computePopupBounds(0, comboBox
//							.getBounds().height, popupSize.width, popupSize.height);
//					scroller.setMaximumSize(popupBounds.getSize());
//					scroller.setPreferredSize(popupBounds.getSize());
//					scroller.setMinimumSize(popupBounds.getSize());
//					list.invalidate();
//					int selectedIndex = comboBox.getSelectedIndex();
//					if (selectedIndex == -1) {
//						list.clearSelection();
//					} else {
//						list.setSelectedIndex(selectedIndex);
//					}
//					list.ensureIndexIsVisible(list.getSelectedIndex());
//					setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
//
//					show(comboBox, popupBounds.x, popupBounds.y);
//				}
//			};
//			popup.getAccessibleContext().setAccessibleParent(comboBox);
//			return popup;
//		}
//	}
//
//}