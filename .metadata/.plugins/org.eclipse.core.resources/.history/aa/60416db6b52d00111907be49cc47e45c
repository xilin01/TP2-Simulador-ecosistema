package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.json.JSONArray;
import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class RegionManager implements AnimalMapView {
	private int width;
	private int height;
	private int cols;
	private int rows;
	private int regionWidth;
	private int regionHeight;
	private List<List<Region>> regions;
	private Map<Animal, Region> animalRegion;
	
	public RegionManager(int cols, int rows, int width, int height) {
		this.width = width;
		this.height = height;
		this.cols = cols; 
		this.rows = rows;
		regionWidth = width / cols; 
		regionHeight = height / rows;
		regions = new ArrayList<>();
		for(int i = 0; i < rows; i++) {
			List<Region> aux = new ArrayList<>();
			for(int j = 0; j < cols; j++) aux.add(new DefaultRegion());
			regions.add(aux);
		}
		animalRegion = new HashMap<>();
	}
	
	@Override
	public int getCols() {
		return cols;
	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getRegionWidth() {
		return regionWidth;
	}	

	@Override
	public int getRegionHeight() {
		return regionHeight;
	}
	
	void setRegion(int row, int col, Region r) {
		for(Animal a: regions.get(row).get(col).getAnimals()) {
			r.addAnimal(a);
			animalRegion.replace(a, r);
		}
		regions.get(row).set(col, r);
	}
	
	void registerAnimal(Animal a) {
		a.init(this);
		Region aux = findRegion(a.getPosition());
		aux.addAnimal(a);
		animalRegion.put(a, aux);
	}
	
	private Region findRegion(Vector2D pos) {
		int row = (int) Utils.constrainValueInRange(pos.getX() / regionWidth, 0, rows - 1);
		int col = (int) Utils.constrainValueInRange(pos.getY() / regionHeight, 0, cols - 1);

		return regions.get(row).get(col);
	}
	
	void unregisterAnimal(Animal a) {
		animalRegion.remove(a).removeAnimal(a);
	}
	
	void updateAnimalRegion(Animal a) {
		Region act = findRegion(a.getPosition()), ant = animalRegion.get(a);
		
		if(act != ant) {
			if(ant != null) ant.removeAnimal(a);
			act.addAnimal(a);
			animalRegion.put(a, act);
		}
	}
	
	@Override
	public double getFood(AnimalInfo a, double dt) {
		return animalRegion.get(a).getFood(a, dt);	
	}
	
	void updateAllRegions(double dt) {
		for(List<Region> regionList: regions) {
			for(Region r: regionList) r.update(dt);
		}
	}		

	@Override
	public List<Animal> getAnimalsInRange(Animal a, Predicate<Animal> filter) {
		List<Animal> aux = new ArrayList<>();
		int row1 = (int) Utils.constrainValueInRange((a.getPosition().getX() - a.getSightRange()) / regionWidth, 0, rows - 1);
		int row2 = (int) Utils.constrainValueInRange((a.getPosition().getX() + a.getSightRange()) / regionWidth, 0, rows - 1);
		int col1 = (int) Utils.constrainValueInRange((a.getPosition().getY() - a.getSightRange()) / regionHeight, 0, cols - 1);
		int col2 = (int) Utils.constrainValueInRange((a.getPosition().getY() + a.getSightRange()) / regionHeight, 0, cols - 1);
		
		for(int i = row1; i <= row2; i++) {
			for(int j = col1; j <= col2; j++) {
				for(Animal animal: regions.get(i).get(j).getAnimals()) {
					if(a.distanceTo(animal) <= a.getSightRange() && filter.test(animal)) aux.add(animal);
				}	
			}
		}
		aux.remove(a);
		return aux;
	}
	
	@Override
	public JSONObject asJSON() {
		JSONArray ja = new JSONArray();
		
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) ja.put(new JSONObject().put("row", i).put("col", j).put("data", regions.get(i).get(j).asJSON()));
		}
		return new JSONObject().put("regions", ja);
	}
}
