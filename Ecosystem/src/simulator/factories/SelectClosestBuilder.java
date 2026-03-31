package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectClosest;
import simulator.model.SelectionStrategy;

public class SelectClosestBuilder extends Builder<SelectionStrategy> {
	private static final String TYPETAG = "closest";
	private static final String DESC = "creates a SelectClosest selection strategy";
	
	public SelectClosestBuilder() {
		super(TYPETAG, DESC);
	}

	@Override
	protected SelectClosest createInstance(JSONObject data) {
		if(data == null || !data.isEmpty()) throw new IllegalArgumentException("Invalid data for createInstance in SelectClosestBuilder");
		return new SelectClosest();
	}
}
