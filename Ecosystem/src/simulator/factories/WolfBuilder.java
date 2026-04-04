package simulator.factories;

import org.json.JSONObject;
import simulator.misc.Vector2D;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends AnimalBuilder {
	private static final String TYPETAG = "wolf";
	private static final String DESC = "creates a Wolf";

	public WolfBuilder(Factory<SelectionStrategy> selectionStrategyFactory) {
		super(TYPETAG, DESC, selectionStrategyFactory);
	}

	@Override
	protected void fillInDangerStrategy(JSONObject jo) {		
	}

	@Override
	protected void fillInHuntStrategy(JSONObject jo) {
		jo.put("hunt_strategy", "JSONOject description of the hunt selection strategy to build (optional, default SelectFirst)");
	}
	
	@Override
	protected Wolf createInstance(JSONObject data) {
		if(data == null) throw new IllegalArgumentException("Invalid data for createInstance in WolfBuilder");
		return (Wolf)super.createInstance(data);
	}

	@Override
	protected Wolf newInstance(JSONObject data, SelectionStrategy mateStrategy, Vector2D pos) {
		SelectionStrategy huntingStrategy = data.has("hunt_strategy") ? selectionStrategyFactory.createInstance(data.getJSONObject("hunt_strategy")) : new SelectFirst();
		
		return new Wolf(mateStrategy, huntingStrategy, pos);
	}
}
