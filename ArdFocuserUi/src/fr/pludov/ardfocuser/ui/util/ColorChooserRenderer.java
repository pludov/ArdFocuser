package fr.pludov.ardfocuser.ui.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ColorChooserRenderer implements TableCellRenderer {

	public ColorChooserRenderer() {
	}

	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
			int arg5) {
		JButton jb = new JButton();
		if (arg1 instanceof Color) {
			jb.setBackground((Color)arg1);
		}
		return jb;
	}

}
