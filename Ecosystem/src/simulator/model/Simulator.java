package simulator.model;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {
	private Factory<Animal> animalsFactory;
	private Factory<Region> regionsFactory;
	private RegionManager regionManager;
	private List<Animal> animals;
	private double time;
	private List<EcoSysObserver> observers;
	
	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory, Factory<Region> regionsFactory) {
		this.animalsFactory = animalsFactory;
		this.regionsFactory = regionsFactory;
		regionManager = new RegionManager(cols, rows, width, height);
		animals = new ArrayList<>();
		time = 0;
		observers = new ArrayList<>();
	}
		
	private void setRegion(int row, int col, Region r) {
		regionManager.setRegion(row, col, r);
		for(EcoSysObserver o: observers) o.onRegionSet(row, col, regionManager, r);
	}
	
	public void setRegion(int row, int col, JSONObject rJson) {
		setRegion(row, col, regionsFactory.createInstance(rJson));
	}
	
	private void addAnimal(Animal a) {
		if(!animals.contains(a)) {
			animals.add(a);
			regionManager.registerAnimal(a);
			List<AnimalInfo> aux = new ArrayList<>(animals);
			for(EcoSysObserver o: observers) o.onAnimalAdded(time, regionManager, aux, a);
		}
	}
	
	public void addAnimal(JSONObject aJson) {
		addAnimal(animalsFactory.createInstance(aJson));
	}
	
	public MapInfo getMapInfo() {
		return regionManager;
	}
	
	public List<? extends AnimalInfo> getAnimals() {
		return new ArrayList<>(animals);
	}
	
	public double getTime() {
		return time;
	}
	
	public void advance(double dt) {
		time += dt;
		removeDead();
		for(Animal a: animals) {
			a.update(dt);
			regionManager.updateAnimalRegion(a);
		}
		regionManager.updateAllRegions(dt);
		addBabies();
		List<AnimalInfo> aux = new ArrayList<>(animals);
		for(EcoSysObserver o: observers) o.onAdvance(time, regionManager, aux, dt);
	}
	
	private void removeDead() {
		List<Animal> aux = new ArrayList<>();
		
		for(Animal a: animals) {
			if(a.getState() == Animal.State.DEAD) regionManager.unregisterAnimal(a);
			else aux.add(a);
		}
		animals = aux;
	}
	
	private void addBabies() {
		List<Animal> aux = new ArrayList<>(animals);
			
		for(Animal a: aux) {
			if(a.isPregnant()) addAnimal(a.deliverBaby());
		}
	}
	
	public void reset(int cols, int rows, int width, int height) {
		animals = new ArrayList<>();
		regionManager = new RegionManager(cols, rows, width, height);
		time = 0;
		List<AnimalInfo> aux = new ArrayList<>();
		for(EcoSysObserver o: observers) o.onReset(time, regionManager, aux);
	}

	@Override
	public void addObserver(EcoSysObserver o) {
		if(!observers.contains(o)) {
			observers.add(o);
			List<AnimalInfo> aux = new ArrayList<>(animals);
			o.onRegister(time, regionManager, aux);
		}
	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		observers.remove(o);
	}
	
	@Override
	public JSONObject asJSON() {
		return new JSONObject().put("time", time).put("state", regionManager.asJSON());
	}
}
