package simulator.factories;

import org.json.JSONObject;
import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {
	private static final String TYPETAG = "default";
	private static final String DESC = "creates a DefaultRegion";
	
	public DefaultRegionBuilder() {
		super(TYPETAG, DESC);
	}

	@Override
	protected DefaultRegion createInstance(JSONObject data) {
		if(data == null || !data.isEmpty()) throw new IllegalArgumentException("Invalid data for createInstance in DefaultRegionBuilder");
		return new DefaultRegion();
	}
}
