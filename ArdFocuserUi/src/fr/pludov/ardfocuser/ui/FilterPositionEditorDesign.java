package fr.pludov.ardfocuser.ui;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JTable;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FilterPositionEditorDesign extends JPanel {
	protected JPanel panel;
	protected JScrollPane scrollPane;
	protected JTable filterDataTable;
	protected JButton btnAdd;
	protected JButton btnDelete;
	protected JButton btnUp;
	protected JButton btnDown;
	protected JButton btnInterpolate;

	/**
	 * Create the panel.
	 */
	public FilterPositionEditorDesign() {
		setLayout(new MigLayout("", "[grow][center]", "[][][][][grow]"));
		
		this.panel = new JPanel();
		add(this.panel, "cell 0 0 1 5,grow");
		this.panel.setLayout(new GridLayout(1, 1, 0, 0));
		
		this.scrollPane = new JScrollPane();
		this.panel.add(this.scrollPane);
		
		this.filterDataTable = new JTable();
		this.filterDataTable.setModel(new DefaultTableModel(
			new Object[][] {
				{"", null, null},
				{null, null, null},
			},
			new String[] {
				"Nom", "Couleur", "Position"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, Object.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		this.filterDataTable.getColumnModel().getColumn(0).setPreferredWidth(147);
		this.filterDataTable.getColumnModel().getColumn(1).setMinWidth(75);
		this.filterDataTable.getColumnModel().getColumn(1).setMaxWidth(75);
		this.filterDataTable.getColumnModel().getColumn(2).setResizable(false);
		this.filterDataTable.getColumnModel().getColumn(2).setMinWidth(75);
		this.filterDataTable.getColumnModel().getColumn(2).setMaxWidth(75);
		this.scrollPane.setViewportView(this.filterDataTable);
		
		this.btnAdd = new JButton("Ajoute");
		add(this.btnAdd, "cell 1 0");
		
		this.btnUp = new JButton("Monter");
		add(this.btnUp, "cell 1 1");
		
		this.btnDown = new JButton("Descendre");
		add(this.btnDown, "cell 1 2");
		
		this.btnDelete = new JButton("Supprime");
		add(this.btnDelete, "cell 1 3");
		
		this.btnInterpolate = new JButton("Interpoler");
		add(this.btnInterpolate, "cell 1 4,aligny top");
		
	}
}
