package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectYoungest;
import simulator.model.SelectionStrategy;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {
	private static final String TYPETAG = "youngest";
	private static final String DESC = "creates a SelectYoungest selection strategy";
	
	public SelectYoungestBuilder() {
		super(TYPETAG, DESC);
	}

	@Override
	protected SelectYoungest createInstance(JSONObject data) {
		if(data == null || !data.isEmpty()) throw new IllegalArgumentException("Invalid data for createInstance in SelectYoungestBuilder");
		return new SelectYoungest();
	}
}
