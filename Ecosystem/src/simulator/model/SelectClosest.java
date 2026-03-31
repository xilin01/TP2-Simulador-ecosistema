package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if(as.isEmpty()) return null;
		Animal closest = as.getFirst();
		for(Animal animal: as) {
			if(a.distanceTo(animal) < a.distanceTo(closest)) closest = animal;
		}
		return closest;
	}
}
