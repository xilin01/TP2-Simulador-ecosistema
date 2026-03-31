package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {
	private static final String TYPETAG = "first";
	private static final String DESC = "creates a SelectFirst selection strategy";
	
	public SelectFirstBuilder() {
		super(TYPETAG, DESC);
	}

	@Override
	protected SelectFirst createInstance(JSONObject data) {
		if(data == null || !data.isEmpty()) throw new IllegalArgumentException("Invalid data for createInstance in SelectFirstBuilder");
		return new SelectFirst();
	}
}
