package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {
	final static String WOLF_GENETIC_CODE = "Wolf";
	final static double INIT_SIGHT_WOLF = 50;
	final static double INIT_SPEED_WOLF = 60;
	final static double BOOST_FACTOR_WOLF = 3.0;
	final static double MAX_AGE_WOLF = 14.0;
	final static double FOOD_THRSHOLD_WOLF = 50.0;
	final static double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
	final static double FOOD_DROP_RATE_WOLF = 18.0;
	final static double FOOD_DROP_DESIRE_WOLF = 10.0;
	final static double FOOD_EAT_VALUE_WOLF = 50.0;
	final static double DESIRE_THRESHOLD_WOLF = 65.0;
	final static double DESIRE_INCREASE_RATE_WOLF = 30.0;
	final static double PREGNANT_PROBABILITY_WOLF = 0.75;
	
	private Animal huntTarget;
	private SelectionStrategy huntingStrategy;
	
	public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy,  Vector2D pos) {
		super(WOLF_GENETIC_CODE, Diet.CARNIVORE, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);
		this.huntingStrategy = huntingStrategy;	
	}
	
	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		huntTarget = null;
		huntingStrategy = p1.getHuntingStrategy();
	}
	
	private SelectionStrategy getHuntingStrategy() {
		return huntingStrategy;
	}
	
	@Override
	protected void normalUpdateEnergyDesire(double dt) {
		updateEnergyDesire(FOOD_DROP_RATE_WOLF * dt, DESIRE_INCREASE_RATE_WOLF * dt);
	}
	
	@Override
	protected void updateDangerSource() {
	}
	
	@Override
	protected void normalStateUpdate() {
		if(energy < FOOD_THRSHOLD_WOLF) setState(State.HUNGER);
		else if(desire > DESIRE_THRESHOLD_WOLF) setState(State.MATE);
	}
	
	@Override
	protected void dangerState(double dt) {
		assert(false);
	}
	
	@Override
	protected void hungerState(double dt) {
		if(huntTarget == null || (huntTarget != null && (huntTarget.getState() == State.DEAD || distanceTo(huntTarget) > sightRange))) 
			huntTarget = huntingStrategy.select(this, regionManager.getAnimalsInRange(this, a -> a.getDiet() == Diet.HERBIVORE));
		if(huntTarget == null) normalAdvance(dt);
		else {
			dest = huntTarget.getPosition();
			anormalAdvance(dt);
			if(distanceTo(huntTarget) < COLLISION_RANGE) {
				huntTarget.setState(State.DEAD);
				huntTarget = null;
				energy = energy + FOOD_EAT_VALUE_WOLF >= MAX_ENERGY ? MAX_ENERGY : energy + FOOD_EAT_VALUE_WOLF;
			}
		}
		if(energy > FOOD_THRSHOLD_WOLF) {
			if(desire < DESIRE_THRESHOLD_WOLF) setState(State.NORMAL);
			else setState(State.MATE);
		}
	}
	
	@Override
	protected void anormalAdvance(double dt) {
		move(BOOST_FACTOR_WOLF*speed*dt*Math.exp((energy-MAX_ENERGY)*HUNGER_DECAY_EXP_FACTOR));
		age += dt;
		updateEnergyDesire(FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt, DESIRE_INCREASE_RATE_WOLF * dt);
	}
	
	@Override
	protected void impregnate() {
		if(!isPregnant() && Utils.RAND.nextDouble() <= PREGNANT_PROBABILITY_WOLF) baby = new Wolf(this, mateTarget);
		energy = energy - FOOD_DROP_DESIRE_WOLF <= 0 ? 0 : energy - FOOD_DROP_DESIRE_WOLF;
	}
	
	@Override
	protected void mateStateUpdate() {
		if(energy < FOOD_THRSHOLD_WOLF) setState(State.HUNGER);
		else if(desire < DESIRE_THRESHOLD_WOLF) setState(State.NORMAL);
	}

	@Override
	protected void setNormalStateAction() {
		huntTarget = mateTarget = null;
	}

	@Override
	protected void setMateStateAction() {
		huntTarget = null;
	}

	@Override
	protected void setHungerStateAction() {
		mateTarget = null;
	}

	@Override
	protected void setDangerStateAction() {
		assert(false);
	}

	@Override
	protected void setDeadStateAction() {
		huntTarget = mateTarget = null;
	}
	
	@Override
	protected void checkDead() {
		if(energy == 0 || age > MAX_AGE_WOLF) setState(State.DEAD);
	}
}
