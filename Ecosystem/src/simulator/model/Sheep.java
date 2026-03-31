package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {
	final static String SHEEP_GENETIC_CODE = "Sheep";
	final static double INIT_SIGHT_SHEEP = 40;
	final static double INIT_SPEED_SHEEP = 35;
	final static double BOOST_FACTOR_SHEEP = 2.0;
	final static double MAX_AGE_SHEEP = 8;
	final static double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
	final static double FOOD_DROP_RATE_SHEEP = 20.0;
	final static double DESIRE_THRESHOLD_SHEEP = 65.0;
	final static double DESIRE_INCREASE_RATE_SHEEP = 40.0;
	final static double PREGNANT_PROBABILITY_SHEEP = 0.9;
	
	private Animal dangerSource;
	private SelectionStrategy dangerStrategy;
	
	public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy,  Vector2D pos) {
		super(SHEEP_GENETIC_CODE, Diet.HERBIVORE, INIT_SIGHT_SHEEP, INIT_SPEED_SHEEP, mateStrategy, pos);
		this.dangerStrategy = dangerStrategy;	
	}
	
	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		dangerSource = null;
		dangerStrategy = p1.getDangerStrategy();
	}
	
	private SelectionStrategy getDangerStrategy() {
		return dangerStrategy;
	}
	
	@Override
	protected void normalUpdateEnergyDesire(double dt) {
		updateEnergyDesire(FOOD_DROP_RATE_SHEEP * dt, DESIRE_INCREASE_RATE_SHEEP * dt);
	}
	
	@Override
	protected void updateDangerSource() {
		if(dangerSource == null) dangerSource = dangerStrategy.select(this, regionManager.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE));
	}
	
	@Override
	protected void normalStateUpdate() {
		if(dangerSource != null) setState(State.DANGER);
		else if(desire > DESIRE_THRESHOLD_SHEEP) setState(State.MATE);
	}
	
	@Override
	protected void dangerState(double dt) {
		if(dangerSource != null && dangerSource.getState() == State.DEAD) dangerSource = null;
		if(dangerSource == null) normalAdvance(dt);
		else {
			dest = pos.plus(pos.minus(dangerSource.getPosition()).direction());
			anormalAdvance(dt);
		}
		if(dangerSource == null || distanceTo(dangerSource) > sightRange) {
			dangerSource = dangerStrategy.select(this, regionManager.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE));
			if(dangerSource == null) {
				if(desire < DESIRE_THRESHOLD_SHEEP) setState(State.NORMAL);
				else setState(State.MATE);
			}
		}
	}
	
	@Override
	protected void anormalAdvance(double dt) {
		move(BOOST_FACTOR_SHEEP*speed*dt*Math.exp((energy-MAX_ENERGY)*HUNGER_DECAY_EXP_FACTOR));
		age += dt;
		updateEnergyDesire(FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt, DESIRE_INCREASE_RATE_SHEEP * dt);
	}
	
	@Override
	protected void hungerState(double dt) {
		assert(false);
	}
	
	@Override
	protected void impregnate() {
		if(!isPregnant() && Utils.RAND.nextDouble() <= PREGNANT_PROBABILITY_SHEEP) baby = new Sheep(this, mateTarget);
	}
	
	@Override
	protected void mateStateUpdate() {
		if(dangerSource != null) setState(State.DANGER);
		else if(desire < DESIRE_THRESHOLD_SHEEP) setState(State.NORMAL);
	}

	@Override
	protected void setNormalStateAction() {
		dangerSource = mateTarget = null;
	}

	@Override
	protected void setMateStateAction() {
		dangerSource = null;
	}

	@Override
	protected void setHungerStateAction() {
		assert(false);
	}

	@Override
	protected void setDangerStateAction() {
		mateTarget = null;
	}

	@Override
	protected void setDeadStateAction() {
		dangerSource = mateTarget = null;
	}
	
	@Override
	protected void checkDead() {
		if(energy == 0 || age > MAX_AGE_SHEEP) setState(State.DEAD);
	}
}
