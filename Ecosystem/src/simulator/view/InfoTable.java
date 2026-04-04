package simulator.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class InfoTable extends JPanel {
	private String title;
	private TableModel tableModel;

	public InfoTable(String title, TableModel tableModel) {
		this.title = title;
		this.tableModel = tableModel;
		initGUI();
	}

	private void initGUI() {
		super.setLayout(new BorderLayout());
		super.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3), title));
		JTable table = new JTable(tableModel);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
		super.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
	}
}
