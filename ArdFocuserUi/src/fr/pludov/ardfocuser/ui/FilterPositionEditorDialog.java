package fr.pludov.ardfocuser.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import fr.pludov.ardfocuser.driver.*;
import fr.pludov.ardfocuser.ui.util.*;
import fr.pludov.ardfocuser.ui.util.DialogUtils.*;

public class FilterPositionEditorDialog extends JDialog {
	FilterPositionEditorDesign editor;
	Focuser focuser;
	DefaultTableModel tableModel;
	
	public FilterPositionEditorDialog(Window arg0) {
		super(arg0);
		getContentPane().setLayout(new BorderLayout());
		
		getContentPane().add(editor = new FilterPositionEditorDesign(), BorderLayout.CENTER);
		DialogUtils.buildOkCancel(this, new DialogValidator() {
			@Override
			public boolean close() {
				save();
				return true;
			}
		});
		editor.filterDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableModel = ((DefaultTableModel) editor.filterDataTable.getModel());
		
		editor.btnAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.addRow(new Object[]{"red", Color.red, 100} );
			}
		});
		
		editor.btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int currentRow = editor.filterDataTable.getSelectedRow();
				if (currentRow == -1) {
					return;
				}
				editor.filterDataTable.removeEditor();
				tableModel.removeRow(currentRow);
			}
		});
		
		editor.btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int currentRow = editor.filterDataTable.getSelectedRow();
				if (currentRow < 1) {
					return;
				}
				Object[] rowData = getRowData(currentRow - 1);
				tableModel.removeRow(currentRow - 1);
				tableModel.insertRow(currentRow, rowData);
				
			}
		});
		
		editor.btnDown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int currentRow = editor.filterDataTable.getSelectedRow();
				if (currentRow == -1 || currentRow >= tableModel.getRowCount() - 1) {
					return;
				}

				Object[] rowData = getRowData(currentRow + 1);
				tableModel.removeRow(currentRow + 1);
				tableModel.insertRow(currentRow, rowData);
			}
		});
		
		editor.btnInterpolate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int count;
				if ((count = tableModel.getRowCount()) < 3) {
					return;
				}
				editor.filterDataTable.removeEditor();

				int min = (Integer)tableModel.getValueAt(0, 2);
				int max = (Integer)tableModel.getValueAt(count - 1, 2);
				
				for(int i = 1; i < count - 1; ++i) {
					int val = min + ((max - min) * i)/ (count - 1);
					tableModel.setValueAt(val, i, 2);
				}
			}
			
		});

	    TableColumn column = editor.filterDataTable.getColumnModel().getColumn(1);

	    column.setCellEditor(new ColorChooserEditor());
	    column.setCellRenderer(new ColorChooserRenderer());
	    pack();

	}
	
	Object [] getRowData(int row)
	{
		Object [] result = new Object[tableModel.getColumnCount()];
		for(int i = 0; i < result.length; ++i)
		{
			result[i] = tableModel.getValueAt(row, i);
		}
		return result;
	}
	
	void open(Focuser focuser)
	{
		this.focuser = focuser;
		tableModel.setRowCount(focuser.getFilterDefinitions().size());
		int i = 0;
		for(FilterDefinition fd : focuser.getFilterDefinitions())
		{
			editor.filterDataTable.setValueAt(fd.getName(), i, 0);
			editor.filterDataTable.setValueAt(fd.getColor(), i, 1);
			editor.filterDataTable.setValueAt(fd.getPosition(), i, 2);
			i++;
		}
		
	}
	
	void save()
	{
		List<FilterDefinition> result = new ArrayList<>();
		for(int r = 0; r < tableModel.getRowCount(); ++r)
		{
			FilterDefinition fd = new FilterDefinition();
			
			fd.setName((String)tableModel.getValueAt(r, 0));
			fd.setColor((Color)tableModel.getValueAt(r, 1));
			fd.setPosition((Integer)tableModel.getValueAt(r, 2));
			
			result.add(fd);
		}
		
		focuser.getFilterDefinitions().clear();
		focuser.getFilterDefinitions().addAll(result);
		focuser.saveFilterDefinitions();
	}
	
}
