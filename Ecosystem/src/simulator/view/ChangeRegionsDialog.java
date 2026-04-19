package simulator.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.json.JSONArray;
import org.json.JSONObject;
import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
class ChangeRegionsDialog extends JDialog implements EcoSysObserver {
	private DefaultComboBoxModel<String> regionsModel;
	private DefaultComboBoxModel<String> fromRowModel;
	private DefaultComboBoxModel<String> toRowModel;
	private DefaultComboBoxModel<String> fromColModel;
	private DefaultComboBoxModel<String> toColModel;

	private DefaultTableModel dataTableModel;
	private Controller ctrl;
	private List<JSONObject> regionsInfo;
	private String[] headers = {"Key", "Value", "Description"};
	private int status; // 0 = Cancel, 1 = OK

	ChangeRegionsDialog(Controller ctrl) {
		super((Frame)null, true);
		this.ctrl = ctrl;
		status = 0;
		initGUI();
		ctrl.addObserver(this);
	}

	private void initGUI() {
		super.setTitle("Change Regions");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		super.setContentPane(mainPanel);

		JPanel helpPanel = new JPanel(new BorderLayout());
		JLabel helpLabel = new JLabel("<html><p>Select a region type, the rows/cols interval, and provide values for the parameters in the <b>Value column</b> (default values are used for parameters with no value).</p></html>");
		helpPanel.add(helpLabel, BorderLayout.CENTER);
		mainPanel.add(helpPanel);

		regionsInfo = Main.regionsFactory.getInfo();
		dataTableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};
		dataTableModel.setColumnIdentifiers(headers);
		JTable table = new JTable(dataTableModel) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) { // We override prepareRenderer to resize columns to fit to content
				Component component = super.prepareRenderer(renderer, row, column);
				
				int rendererWidth = component.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);
				tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
				return component;
			}
		};
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
		mainPanel.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		JPanel comboPanel = new JPanel();
		comboPanel.add(new JLabel("Region type:"));
		regionsModel = new DefaultComboBoxModel<>();
		for(JSONObject jo: regionsInfo) regionsModel.addElement(jo.getString("type"));
		JComboBox<String> regionsCombo = new JComboBox<>(regionsModel);
		regionsCombo.addActionListener(e -> updateTableModel(regionsCombo.getSelectedIndex()));
		updateTableModel(0);
		comboPanel.add(regionsCombo);
		fromRowModel = new DefaultComboBoxModel<>();
		toRowModel = new DefaultComboBoxModel<>();
		fromColModel = new DefaultComboBoxModel<>();
		toColModel = new DefaultComboBoxModel<>();
		comboPanel.add(new JLabel("Row from/to:"));
		comboPanel.add(new JComboBox<>(fromRowModel));
		comboPanel.add(new JComboBox<>(toRowModel));
		comboPanel.add(new JLabel("Column from/to:"));
		comboPanel.add(new JComboBox<>(fromColModel));
		comboPanel.add(new JComboBox<>(toColModel));
		mainPanel.add(comboPanel);

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			status = 0;
			super.setVisible(false);
		});
		buttonsPanel.add(cancelButton);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(e -> {
			try {
				ctrl.setRegions(getJSON(regionsCombo.getSelectedIndex()));
				status = 1;
				super.setVisible(false);
			}
			catch(Exception ex) {
				ViewUtils.showErrorMsg(ex.getMessage());
			}
		});
		buttonsPanel.add(okButton);
		mainPanel.add(buttonsPanel);

		super.setPreferredSize(new Dimension(700, 400));
		super.pack();
		super.setResizable(false);
		super.setVisible(false);
	}

	private void updateTableModel(int selectedIndex) {
		dataTableModel.setRowCount(0);
		if(0 <= selectedIndex && selectedIndex < regionsInfo.size()) {
			JSONObject data = regionsInfo.get(selectedIndex).getJSONObject("data");
			
			for(String key: data.keySet()) dataTableModel.addRow(new Object[]{key, "", data.getString(key)});
		}
	}

	private JSONObject getJSON(int selectedIndex) throws Exception {
		JSONObject spec = new JSONObject();
		
		spec.put("type", regionsInfo.get(selectedIndex).getString("type"));
		JSONObject data = new JSONObject();
		for(int i = 0; i < dataTableModel.getRowCount(); i++) {
			String key = dataTableModel.getValueAt(i, 0).toString();
			String value = dataTableModel.getValueAt(i, 1).toString();
			
			if(!value.isEmpty()) data.put(key, value);
		}
		spec.put("data", data);
		JSONObject aux = new JSONObject();
		aux.put("row", new JSONArray().put(fromRowModel.getSelectedItem()).put(toRowModel.getSelectedItem()));
		aux.put("col", new JSONArray().put(fromColModel.getSelectedItem()).put(toColModel.getSelectedItem()));
		aux.put("spec", spec);
		return new JSONObject().put("regions", new JSONArray().put(aux));
	}

	public void open(Frame parent) {
		super.setLocation(parent.getLocation().x + parent.getWidth() / 2 - super.getWidth() / 2, parent.getLocation().y + parent.getHeight() / 2 - super.getHeight() / 2);
		super.pack();
		super.setVisible(true);
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateDimensionModels(map.getRows(), map.getCols());
	}
	
	private void updateDimensionModels(int rows, int cols) {
		fromRowModel.removeAllElements();
		toRowModel.removeAllElements();
		fromColModel.removeAllElements();
		toColModel.removeAllElements();
		for(int i = 0; i < rows; i++) {
			fromRowModel.addElement(String.valueOf(i));
			toRowModel.addElement(String.valueOf(i));
		}
		for(int i = 0; i < cols; i++) {
			fromColModel.addElement(String.valueOf(i));
			toColModel.addElement(String.valueOf(i));
		}
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateDimensionModels(map.getRows(), map.getCols());
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
	}
}
