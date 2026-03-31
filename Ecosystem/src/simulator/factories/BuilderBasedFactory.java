package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
	private Map<String, Builder<T>> builders;
	private List<JSONObject> buildersInfo;

	public BuilderBasedFactory() {  
		builders = new HashMap<>();
		buildersInfo = new LinkedList<>();
	}

	public BuilderBasedFactory(List<Builder<T>> builders) {  
		this();
		for(Builder<T> b: builders) addBuilder(b);
	}

	public void addBuilder(Builder<T> b) {  
		builders.put(b.getTypeTag(), b);
		buildersInfo.add(b.getInfo());
	}

	@Override  
	public T createInstance(JSONObject info) {  
		if (info == null) throw new IllegalArgumentException("’info’ cannot be null");
		if(builders.containsKey(info.getString("type"))) {
			T t = builders.get(info.getString("type")).createInstance(info.has("data") ? info.getJSONObject("data") : new JSONObject());
			
			if(t != null) return t;
		}
		throw new IllegalArgumentException("Unrecognized ‘info’:" + info.toString());
	}

	@Override  
	public List<JSONObject> getInfo() {
		return Collections.unmodifiableList(buildersInfo);
	}
}
