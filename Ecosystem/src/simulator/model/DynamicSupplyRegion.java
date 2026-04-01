package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {
	final static double FOOD_EAT_RATE_HERBS = 60.0;
	final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;
	final static double FOOD_INCREASE_PROBABILITY = 0.5;
	
	private double food;
	private double factor;
	
	public DynamicSupplyRegion(double food, double factor) {
		if(food < 0 || factor < 0) throw new IllegalArgumentException("Invalid arguments for DynamicSupplyRegion builder"); 
		this.food = food;
		this.factor = factor;
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		double food = a.getDiet() == Diet.CARNIVORE ? 0 : Math.min(this.food,FOOD_EAT_RATE_HERBS*Math.exp(-Math.max(0,countHerbivores()-FOOD_SHORTAGE_TH_HERBS)*FOOD_SHORTAGE_EXP_HERBS)*dt);
		
		this.food -= food;
		return food;
	}
	
	@Override
	public void update(double dt) {
		if(Utils.RAND.nextDouble() <= FOOD_INCREASE_PROBABILITY) food += dt * factor;
	}
}
