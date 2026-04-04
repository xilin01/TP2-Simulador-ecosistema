package simulator.model;

import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {
	public enum Diet {CARNIVORE, HERBIVORE;}
	public enum State {NORMAL, MATE, HUNGER, DANGER, DEAD;}
	
	final static double INIT_ENERGY = 100.0;
	final static double MUTATION_TOLERANCE = 0.2;
	final static double NEARBY_FACTOR = 60.0;
	final static double COLLISION_RANGE = 8;
	final static double HUNGER_DECAY_EXP_FACTOR = 0.007;
	final static double MAX_ENERGY = 100.0;
	final static double MAX_DESIRE = 100.0;
	
	protected String geneticCode;
	protected Diet diet;
	protected State state;
	protected Vector2D pos;
	protected Vector2D dest;
	protected double energy;
	protected double speed;
	protected double age;
	protected double desire;
	protected double sightRange;
	protected Animal mateTarget;
	protected Animal baby;
	protected AnimalMapView regionManager;
	protected SelectionStrategy mateStrategy;
	
	protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed, SelectionStrategy mateStrategy, Vector2D pos) {
		if(geneticCode == null || geneticCode.isBlank() || sightRange <= 0 || initSpeed <= 0 || mateStrategy == null) 
			throw new IllegalArgumentException("Invalid arguments for Animal builder");
		this.geneticCode = geneticCode;
		this.diet = diet;
		state = State.NORMAL;
		this.pos = pos;
		dest = null;
		energy = INIT_ENERGY;
		speed = Utils.getRandomizedParameter(initSpeed, 0.1);
		age = 0;
		desire = 0;
		this.sightRange = sightRange;
		mateTarget = null;
		baby = null;
		regionManager = null;
		this.mateStrategy = mateStrategy;
	}
	
	protected Animal(Animal p1, Animal p2) {
		this(p1.getGeneticCode(), p1.getDiet(), Utils.getRandomizedParameter((p1.getSightRange()+p2.getSightRange())/2, MUTATION_TOLERANCE),
				Utils.getRandomizedParameter((p1.getSpeed()+p2.getSpeed())/2, MUTATION_TOLERANCE), p2.getMateStrategy(), 
				p1.getPosition().plus(Vector2D.getRandomVector(-1,1).scale(NEARBY_FACTOR*(Utils.RAND.nextGaussian()+1))));
		energy = (p1.getEnergy() + p2.getEnergy()) / 2;
	}
	
	private SelectionStrategy getMateStrategy() {
		return mateStrategy;
	}
	
	public State getState() {
		return state;
	}
	
	public Vector2D getPosition() {
		return new Vector2D(pos);
	}
	
	public String getGeneticCode() {
		return geneticCode;
	}
	
	public Diet getDiet() {
		return diet;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public double getSightRange() {
		return sightRange;
	}
	
	public double getEnergy() {
		return energy;
	}
	
	public double getAge() {
		return age;
	}
	
	public Vector2D getDestination() {
		return new Vector2D(dest);
	}
	
	public boolean isPregnant() {
		return baby != null;
	}
	
	public void setDesire(double desire) {
		this.desire = desire;
	}
	
	void init(AnimalMapView regMngr) {
		regionManager = regMngr;
		if(pos == null) pos = Vector2D.getRandomVector(0, regionManager.getWidth() - 1, 0, regionManager.getHeight() - 1);
		else pos = pos.adjust(regionManager.getWidth(), regionManager.getHeight());
		dest = Vector2D.getRandomVector(0, regionManager.getWidth() - 1, 0, regionManager.getHeight() - 1);
	}
	
	Animal deliverBaby() {
		Animal a = baby;
		
		baby = null;
		return a;
	}
	
	protected void move(double speed) {
		pos = pos.plus(dest.minus(pos).direction().scale(speed));
	}
	
	protected void setState(State state) {
		this.state = state;
		switch(state) {
			case NORMAL: setNormalStateAction(); break;
			case MATE: setMateStateAction(); break;
			case HUNGER: setHungerStateAction(); break;
			case DANGER: setDangerStateAction(); break;
			case DEAD: setDeadStateAction(); break;
		}
	}
	
	public double distanceTo(Animal a) {
		return pos.distanceTo(a.getPosition());
	}
	
	@Override
	public void update(double dt) {
		if(state != State.DEAD) {
			switch(state) {
				case NORMAL: normalState(dt); break;
				case DANGER: dangerState(dt); break;
				case HUNGER: hungerState(dt); break;
				case MATE: mateState(dt); break;
				default:
			}
			if(!pos.isValid(regionManager.getWidth(), regionManager.getHeight())) {
				pos = pos.adjust(regionManager.getWidth(), regionManager.getHeight());
				setState(State.NORMAL);
			}
			checkDead();
			if(state != State.DEAD) {
				energy += regionManager.getFood(this, dt);
				if(energy > MAX_ENERGY) energy = MAX_ENERGY;
			}
		}
	}
	
	protected void normalState(double dt) {
		normalAdvance(dt);
		updateDangerSource();
		normalStateUpdate();
	}
	
	protected void normalAdvance(double dt) {
		if(pos.distanceTo(dest) < COLLISION_RANGE) dest = Vector2D.getRandomVector(0, regionManager.getWidth() - 1, 0, regionManager.getHeight() - 1);
		move(speed*dt*Math.exp((energy-MAX_ENERGY)*HUNGER_DECAY_EXP_FACTOR));
		age += dt;
		normalUpdateEnergyDesire(dt);
	}
	
	protected void updateEnergyDesire(double energyDrop, double desireIncrease) {
		energy = energy - energyDrop <= 0 ? 0 : energy - energyDrop;
		desire = desire + desireIncrease >= MAX_DESIRE ? MAX_DESIRE : desire + desireIncrease;
	}

	protected void mateState(double dt) {
		if(mateTarget != null && (mateTarget.getState() == State.DEAD || distanceTo(mateTarget) > sightRange)) mateTarget = null;
		if(mateTarget == null) {
			mateTarget = mateStrategy.select(this, regionManager.getAnimalsInRange(this, a -> a.getGeneticCode().equals(this.getGeneticCode())));
			if(mateTarget == null) normalAdvance(dt);
		}
		else {
			dest = mateTarget.getPosition();
			anormalAdvance(dt);
			if(distanceTo(mateTarget) < COLLISION_RANGE) {
				desire = 0;
				mateTarget.setDesire(0);
				impregnate();
				mateTarget = null;
			}
		}
		updateDangerSource();
		mateStateUpdate();
	}
	
	@Override
	public JSONObject asJSON() {
		return new JSONObject().put("pos", pos.asJSONArray()).put("gcode", geneticCode).put("diet", diet).put("state", state);
	}
	
	abstract protected void setNormalStateAction();
	abstract protected void setMateStateAction();
	abstract protected void setHungerStateAction();
	abstract protected void setDangerStateAction();
	abstract protected void setDeadStateAction();
	abstract protected void checkDead();
	abstract protected void normalUpdateEnergyDesire(double dt);
	abstract protected void updateDangerSource();
	abstract protected void normalStateUpdate();
	abstract protected void dangerState(double dt);
	abstract protected void hungerState(double dt);
	abstract protected void anormalAdvance(double dt);
	abstract protected void impregnate();
	abstract protected void mateStateUpdate();
}
