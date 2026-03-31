package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if(as.isEmpty()) return null;
		Animal youngest = as.getFirst();
		for(Animal animal: as) {
			if(animal.getAge() < youngest.getAge()) youngest = animal;
		}
		return youngest;
	}
}
