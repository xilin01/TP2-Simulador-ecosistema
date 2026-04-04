package simulator.view;

import simulator.model.Animal.State;
import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {
	// Width/ height of the simulation -- they will always be equal to the size of the component
	private int width;
	private int height;
	// Number of rows/ cols of the simulation (regions) 
	private int rows;
	private int cols;
	// Width/ height of a region
	int rWidth;
	int rHeight;
	// We show the animals that have this state. The possible values of currState are null and the values returned by State.values(). If it is null we show all animals
	State currState;
	// The value of these attributes are the list of animals and the time that we have received in the notification (those will be shown)
	volatile private Collection<AnimalInfo> objs;
	volatile private Double time;
	// An auxiliary class to store information about species
	private static class SpeciesInfo {
		private Integer count;
		private Color color;

		SpeciesInfo(Color color) {
			count = 0;
			this.color = color;
		}
	}
	// A map with the information for each species
	Map<String, SpeciesInfo> kindsInfo = new HashMap<>();
	// The font to be used for drawing text
	private Font textFont = new Font("Arial", Font.BOLD, 15);
	// Indicates if the 'help' information is visible or hidden
	private boolean showHelp;

	public MapViewer() {
		initGUI();
	}

	private void initGUI() {
		super.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyChar()) {
					case 'h':
						showHelp = !showHelp;
						repaint();
						break;
					case 's':
						if(currState == null) currState = State.values()[0];
						else {
							int nextIndex = currState.ordinal() + 1;
							
							if(nextIndex < State.values().length) currState = State.values()[nextIndex];
							else currState = null;
						}
						repaint();
						break;
					default:
				}
			}
		});
		super.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); // This is needed to capture keystrokes when the mouse is over this component 
			}
		});
		currState = null;
		showHelp = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gr = (Graphics2D)g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(textFont);
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, width, height);
		if(objs != null) drawObjects(gr, objs, time);
		if(showHelp) drawHelp(gr);
	}
	
	private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {
		drawGrid(g);
		drawAnimals(g, animals);
		drawInfo(g, time);
	}

	private void drawGrid(Graphics2D g) {
		g.setColor(Color.GRAY);
		for(int r = 0; r <= rows; r++) g.drawLine(0, r * rHeight, width, r * rHeight);
		for(int c = 0; c <= cols; c++) g.drawLine(c * rWidth, 0, c * rWidth, height);
	}

	private void drawAnimals(Graphics2D g, Collection<AnimalInfo> animals) {
		for(AnimalInfo a: animals) {
			if(!visible(a)) continue;
			String species = a.getGeneticCode();
			SpeciesInfo speciesInfo = kindsInfo.get(species);
			if(speciesInfo == null) {
				speciesInfo = new SpeciesInfo(ViewUtils.getColor(species));
				kindsInfo.put(species, speciesInfo);
			}
			speciesInfo.count++;
			g.setColor(speciesInfo.color);
			g.fillRect((int)a.getPosition().getX(), (int)a.getPosition().getY(), (int)a.getAge() * 2 + 2, (int)a.getAge() * 2 + 2);
		}
	}
	
	private boolean visible(AnimalInfo a) {
		if(currState == null || a.getState() == currState) return true;
		else return false;
	}

	private void drawInfo(Graphics2D g, Double time) {
		int x = rWidth / 4, y = height - rHeight / 2;
		
		if(currState != null) {
			g.setColor(Color.BLUE);
			drawStringWithRect(g, x, y, "State: " + currState.toString());
			y -= 11 * rHeight / 20;
		}
		g.setColor(Color.PINK);
		drawStringWithRect(g, x, y, String.format("Time: %.3f", time));
		y -= 11 * rHeight / 20;
		for(Entry<String, SpeciesInfo> e: kindsInfo.entrySet()) {
			g.setColor(e.getValue().color);
			drawStringWithRect(g, x, y, e.getKey() + ": " + e.getValue().count);
			y -= 11 * rHeight / 20;
			e.getValue().count = 0;
		}
	}
	
	void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int)rect.getHeight(), (int)rect.getWidth() + 1, (int)rect.getHeight() + 5);
	}
	
	private void drawHelp(Graphics2D g) {
		int x = rWidth / 4, y = rHeight / 2;
		String[] lines = {
			"h: toggle help",
			"s: show animals of a specific state"
		};

		g.setColor(Color.RED);
		for(String s: lines) {
			g.drawString(s, x, y);
			y += rHeight / 3;
		}
	}

	@Override
	public void update(List<AnimalInfo> objs, Double time) {
		this.objs = objs;
		this.time = time;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
		width = map.getWidth();
		height = map.getHeight();
		rows = map.getRows();
		cols = map.getCols();
		rWidth = map.getRegionWidth();
		rHeight = map.getRegionHeight();
		super.setPreferredSize(new Dimension(width, height));
		update(animals, time);
	}
}
