package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {
	private Simulator sim;
	
	public Controller(Simulator sim) {
		this.sim = sim;
	}
	
	public void loadData(JSONObject data) {
		if(data.has("regions")) loadRegions(data);
		loadAnimals(data);
	}
	
	private void loadRegions(JSONObject data) {
		JSONArray ja = data.getJSONArray("regions"), aux;
		JSONObject jo;
		int rf, rt, cf, ct;
		
		for(int i = 0; i < ja.length(); i++) {
			jo = ja.getJSONObject(i);
			aux = jo.getJSONArray("row");
			rf = aux.getInt(0);
			rt = aux.getInt(1);
			aux = jo.getJSONArray("col");
			cf = aux.getInt(0);
			ct = aux.getInt(1);
			jo = jo.getJSONObject("spec");
			for(int R = rf; R <= rt; R++) {
				for(int C = cf; C <= ct; C++) sim.setRegion(R, C, jo);
			}
		}
	}
	
	private void loadAnimals(JSONObject data) {
		JSONArray ja = data.getJSONArray("animals");
		JSONObject jo;
		int N;
		
		for(int i = 0; i < ja.length(); i++) {
			jo = ja.getJSONObject(i);
			N = jo.getInt("amount");
			jo = jo.getJSONObject("spec");
			for(int j = 0; j < N; j++) sim.addAnimal(jo);
		}
	}
	
	public void run(double t, double dt, boolean sv, OutputStream out) {
		SimpleObjectViewer view = null;
		
		if(sv) {  
		   MapInfo m = sim.getMapInfo();
		   
		   view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());
		   view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt); 
		}
		JSONObject jo = new JSONObject().put("in", sim.asJSON());
		while(sim.getTime() <= t) {
			sim.advance(dt);
			if(sv) view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
		}
		PrintStream p = new PrintStream(out);
		p.println(jo.put("out", sim.asJSON()).toString(2));	
		if(sv) view.close();
	}
	
	private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		
		for(AnimalInfo a: animals) ol.add(new ObjInfo(a.getGeneticCode(), (int)a.getPosition().getX(), (int)a.getPosition().getY(), (int)Math.round(a.getAge())+2));
		return ol;
	}
	
	public void reset(int cols, int rows, int width, int height) {
		sim.reset(cols, rows, width, height);
	}
	
	public void setRegions(JSONObject rs) {
		loadRegions(rs);
	}
	
	public void advance(double dt) {
		sim.advance(dt);
	}
	
	public void addObserver(EcoSysObserver o) {
		sim.addObserver(o);
	}
	
	public void removeObserver(EcoSysObserver o) {
		sim.removeObserver(o);
	}
}
