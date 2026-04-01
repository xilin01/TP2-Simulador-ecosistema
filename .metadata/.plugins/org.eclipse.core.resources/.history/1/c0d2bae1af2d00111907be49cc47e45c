package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public abstract class AnimalBuilder extends Builder<Animal> {
	protected Factory<SelectionStrategy> selectionStrategyFactory;

	protected AnimalBuilder(String typeTag, String desc, Factory<SelectionStrategy> selectionStrategyFactory) {
		super(typeTag, desc);
		this.selectionStrategyFactory = selectionStrategyFactory;
	}

	@Override
	protected void fillInData(JSONObject jo) {
		jo.put("mate_strategy", "JSONOject description of the selection strategy to build (optional, if not specified it will use the SelectFirst selection strategy)");
		fillInDangerStrategy(jo);
		fillInHuntStrategy(jo);
		JSONObject aux = new JSONObject();
		aux.put("x_range", "JSONArray description of the range of values [a, b] where the x coordinate will be randomly set");
		aux.put("y_range", "JSONArray description of the range of values [a, b] where the y coordinate will be randomly set");
		jo.put("pos*", aux);
		jo.put("*", "JSONObject description of the position range (optional, if not specified it will use null)");
	}
	
	@Override
	protected Animal createInstance(JSONObject data) {
		SelectionStrategy mateStrategy = data.has("mate_strategy") ? selectionStrategyFactory.createInstance(data.getJSONObject("mate_strategy")) : new SelectFirst();
		Vector2D pos = null;
		
		if(data.has("pos")) {
			JSONObject jo = data.getJSONObject("pos");
			JSONArray ja1 = jo.getJSONArray("x_range"), ja2 = jo.getJSONArray("y_range");
			
			pos = Vector2D.getRandomVector(ja1.getDouble(0), ja1.getDouble(1), ja2.getDouble(0), ja2.getDouble(1));
		}
		return newInstance(data, mateStrategy, pos);
	}
	
	abstract protected void fillInDangerStrategy(JSONObject jo);
	abstract protected void fillInHuntStrategy(JSONObject jo);
	abstract protected Animal newInstance(JSONObject data, SelectionStrategy mateStrategy, Vector2D pos);
}
