package simulator.factories;

import org.json.JSONObject;
import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {
	private static final String TYPETAG = "dynamic";
	private static final String DESC = "Dynamic food supply";
	private final static double INIT_FOOD = 100.0;
	private final static double FACTOR = 2.0;
	
	public DynamicSupplyRegionBuilder() {
		super(TYPETAG, DESC);
	}
	
	@Override
	protected void fillInData(JSONObject jo) {
		jo.put("factor", "food increase factor (optional, default 2.0)");
		jo.put("food", "initial amount of food (optional, default 100.0)");
	}

	@Override
	protected DynamicSupplyRegion createInstance(JSONObject data) {
		if(data == null || data.optDouble("food", INIT_FOOD) < 0 || data.optDouble("factor", FACTOR) < 0) 
			throw new IllegalArgumentException("Invalid data for createInstance in DynamicSupplyRegionBuilder");
		return new DynamicSupplyRegion(data.optDouble("food", INIT_FOOD), data.optDouble("factor", FACTOR));
	}
}
