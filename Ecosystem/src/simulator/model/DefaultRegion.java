package simulator.model;

public class DefaultRegion extends Region {
	final static double FOOD_EAT_RATE_HERBS = 60.0;
	final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;

	@Override
	public double getFood(AnimalInfo a, double dt) {
		return a.getDiet() == Diet.CARNIVORE ? 0 : FOOD_EAT_RATE_HERBS*Math.exp(-Math.max(0,countHerbivores()-FOOD_SHORTAGE_TH_HERBS)*FOOD_SHORTAGE_EXP_HERBS)*dt;
	}
	
	@Override
	public void update(double dt) {
	}

	@Override
	public String toString() {
		return "default";
	}
}
