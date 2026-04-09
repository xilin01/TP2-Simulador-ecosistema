package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Animal.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.MapInfo.RegionData;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {
	private class RegionRowData {
		private int row;
		private int col;
		private String desc;
		private Map<Diet, Integer> dietsCount;

		public RegionRowData(int row, int col, String desc) {
			this.row = row;
			this.col = col;
			this.desc = desc;
			dietsCount = new HashMap<>();
			for(Diet d: Diet.values()) dietsCount.put(d, 0);
		}
	}
	
	private String[] columnNames;
	private List<RegionRowData> tableData;

	RegionsTableModel(Controller ctrl) {
		Diet[] diets = Diet.values();
		
		columnNames = new String[diets.length + 3];
		columnNames[0] = "Row";
		columnNames[1] = "Col";
		columnNames[2] = "Desc.";
		for(int i = 0; i < diets.length; i++) columnNames[i + 3] = diets[i].toString();
		tableData = new ArrayList<>();
		ctrl.addObserver(this);
	}

	@Override
	public int getRowCount() {	
		return tableData.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RegionRowData data = tableData.get(rowIndex);

		switch(columnIndex) {
			case 0: return data.row;
			case 1: return data.col;
			case 2: return data.desc;
			default: return data.dietsCount.get(Diet.values()[columnIndex - 3]);
		}
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(map);
	}
	
	private void updateData(MapInfo map) {
		SwingUtilities.invokeLater(() -> {
			tableData.clear();
			for(RegionData rd: map) {
				RegionRowData rowData = new RegionRowData(rd.row(), rd.col(), rd.r().toString());

				for(AnimalInfo a: rd.r().getAnimalsInfo()) rowData.dietsCount.put(a.getDiet(), rowData.dietsCount.get(a.getDiet()) + 1);
				tableData.add(rowData);
			}
			super.fireTableDataChanged();
		});
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(map);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateData(map);
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		updateData(map);
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateData(map);
	}
}
