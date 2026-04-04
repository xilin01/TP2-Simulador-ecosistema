package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import simulator.control.Controller;
import simulator.model.Animal.State;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {
	private String[] columnNames;
	private List<String> speciesList;
	private Map<String, Map<State, Integer>> speciesData;

	SpeciesTableModel(Controller ctrl) {
		State[] states = State.values();
		
		columnNames = new String[states.length + 1];
		columnNames[0] = "Species";
		for(int i = 0; i < states.length; i++) columnNames[i + 1] = states[i].toString();
		speciesList = new ArrayList<>();
		speciesData = new HashMap<>();
		ctrl.addObserver(this);
	}
	
	@Override
	public int getRowCount() {
		return speciesList.size();
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
		String species = speciesList.get(rowIndex);
		Map<State, Integer> statesCount = speciesData.get(species);

		switch(columnIndex) {
			case 0: return species;
			default: return statesCount.getOrDefault(State.values()[columnIndex - 1], 0);
		}
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		update(animals);
	}
	
	private void update(List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			speciesList.clear();
			speciesData.clear();
			for(AnimalInfo a: animals) update(a);
			super.fireTableDataChanged();
		});
	}
	
	private void update(AnimalInfo a) {
		String species = a.getGeneticCode();
		State state = a.getState();
		
		if(!speciesData.containsKey(species)) {
			speciesList.add(species);
			speciesData.put(species, new HashMap<>());
		}
		Map<State, Integer> statesCount = speciesData.get(species);
		statesCount.put(state, statesCount.getOrDefault(state, 0) + 1);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		update(animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		SwingUtilities.invokeLater(() -> {
			update(a);
			int i = 0;
			boolean found = false;
			while(i < speciesList.size() && !found) {
				if(a.getGeneticCode() == speciesList.get(i)) found = true;
				else i++;
			}
			super.fireTableRowsUpdated(i, i);
		});
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		update(animals);
	}
}
