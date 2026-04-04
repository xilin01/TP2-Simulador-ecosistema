package simulator.factories;

import org.json.JSONObject;
import simulator.misc.Vector2D;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends AnimalBuilder {
	private static final String TYPETAG = "sheep";
	private static final String DESC = "creates a Sheep";

	public SheepBuilder(Factory<SelectionStrategy> selectionStrategyFactory) {
		super(TYPETAG, DESC, selectionStrategyFactory);
	}

	@Override
	protected void fillInDangerStrategy(JSONObject jo) {
		jo.put("danger_strategy", "JSONOject description of the danger selection strategy to build (optional, default SelectFirst)");		
	}

	@Override
	protected void fillInHuntStrategy(JSONObject jo) {
	}

	@Override
	protected Sheep createInstance(JSONObject data) {
		if(data == null) throw new IllegalArgumentException("Invalid data for createInstance in SheepBuilder");
		return (Sheep)super.createInstance(data);
	}
	
	@Override
	protected Sheep newInstance(JSONObject data, SelectionStrategy mateStrategy, Vector2D pos) {
		SelectionStrategy dangerStrategy = data.has("danger_strategy") ? selectionStrategyFactory.createInstance(data.getJSONObject("danger_strategy")) : new SelectFirst();
		
		return new Sheep(mateStrategy, dangerStrategy, pos);
	}
}
