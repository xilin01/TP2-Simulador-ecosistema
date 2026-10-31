package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;
import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;
import simulator.view.MainWindow;
import simulator.view.ViewUtils;

public class Main {
	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String tag;
		private String desc;

		private ExecMode(String modeTag, String modeDesc) {
			tag = modeTag;
			desc = modeDesc;
		}

		public String getTag() {
			return tag;
		}

		public String getDesc() {
			return desc;
		}
		
	    public static ExecMode parseExecMode(String s) throws ParseException {
	        for(ExecMode mode: ExecMode.values()) {
	            if(mode.tag.equals(s)) return mode;
	        }
	        throw new ParseException("Invalid value for mode: " + s);
	    }
	}
	
	// Default values for some parameters
	public final static Double DEFAULT_DELTA_TIME = 0.03; // in seconds
	private final static Double DEFAULT_TIME = 10.0; // in seconds
	private final static ExecMode DEFAULT_MODE = ExecMode.GUI;
	
	// Some attributes to stores values corresponding to command-line parameters
	public static Double deltaTime = null;
	private static String inFile = null;
	private static String outFile = null;
	private static Boolean simpleViewer = false;
	private static Double time = null;
	private static ExecMode mode = null;
	
	// Factories
	public static Factory<Animal> animalsFactory;
	public static Factory<Region> regionsFactory;

	private static void parseArgs(String[] args) {
		Options cmdLineOptions = buildOptions(); // Define the valid command line options
		CommandLineParser parser = new DefaultParser(); // Parse the command line as provided in args
		
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			
			parseDeltaTimeOption(line);
			parseHelpOption(line, cmdLineOptions);
			parseModeOption(line);
			parseInFileOption(line);
			parseOutFileOption(line);
			parseSimpleViewerOption(line);
			parseTimeOption(line);
			// If there are some remaining arguments, then something wrong is provided in the command line!
			String[] remaining = line.getArgs();
			if(remaining.length > 0) {
				String error = "Illegal arguments:";
				
				for(String o: remaining) error += (" " + o);
				throw new ParseException(error);
			}
		}
		catch(ParseException e) {
			ViewUtils.showErrorMsg(e.getMessage());
			System.exit(1);
		}
	}

	private static Options buildOptions() {
		Options cmdLineOptions = new Options();
		
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representing actual time, in seconds, per simulation step. Default value: " + DEFAULT_DELTA_TIME + ".").build()); // Delta time
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build()); // Help
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("Initial configuration file.").build()); // Input file
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg()
				.desc("Execution Mode. Possible values: '" + ExecMode.BATCH.getTag() + "' (" + ExecMode.BATCH.getDesc() + "), '"
						+ ExecMode.GUI.getTag() + "' (" + ExecMode.GUI.getDesc() + "). Default value: '" + DEFAULT_MODE.getTag() + "'.").build()); // Mode
		cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build()); // Output file
		cmdLineOptions.addOption(Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build()); // Simple viewer
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("A real number representing the total simulation time in seconds. Default value: " + DEFAULT_TIME + ".").build()); // Time
		return cmdLineOptions;
	}
	
	private static void parseDeltaTimeOption(CommandLine line) throws ParseException {
		deltaTime = parseTime(line, "dt", "delta-time", DEFAULT_DELTA_TIME);
	}
	
	private static Double parseTime(CommandLine line, String opt, String longOpt, Double defaultValue) throws ParseException {
		String t = line.getOptionValue(opt, defaultValue.toString());
		
		try {
			Double time = Double.parseDouble(t);
			
			assert(time >= 0);
			return time;
		}
		catch(Exception e) {
			throw new ParseException("Invalid value for " + longOpt + ": " + t);
		}
	}

	private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
		if(line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parseInFileOption(CommandLine line) throws ParseException {
		inFile = line.getOptionValue("i");
		if(mode == ExecMode.BATCH && inFile == null) throw new ParseException("In batch mode an input configuration file is required");
	}
	
	private static void parseModeOption(CommandLine line) throws ParseException {
		mode = ExecMode.parseExecMode(line.getOptionValue("m", DEFAULT_MODE.getTag()));
	}
	
	private static void parseOutFileOption(CommandLine line) {
		outFile = line.getOptionValue("o");
	}
	
	private static void parseSimpleViewerOption(CommandLine line) {
		simpleViewer = line.hasOption("sv");
	}

	private static void parseTimeOption(CommandLine line) throws ParseException {
		time = parseTime(line, "t", "time", DEFAULT_TIME);
	}

	private static void initFactories() {
		List<Builder<SelectionStrategy>> selectionStrategyBuilders = new ArrayList<>();  
		List<Builder<Animal>> animalBuilders = new ArrayList<>();
		List<Builder<Region>> regionBuilders = new ArrayList<>();
		
		selectionStrategyBuilders.add(new SelectFirstBuilder());
		selectionStrategyBuilders.add(new SelectClosestBuilder());
		selectionStrategyBuilders.add(new SelectYoungestBuilder());
		Factory<SelectionStrategy> selectionStrategyFactory = new BuilderBasedFactory<SelectionStrategy>(selectionStrategyBuilders);
		animalBuilders.add(new SheepBuilder(selectionStrategyFactory));
		animalBuilders.add(new WolfBuilder(selectionStrategyFactory));
		animalsFactory = new BuilderBasedFactory<Animal>(animalBuilders);
		regionBuilders.add(new DefaultRegionBuilder());
		regionBuilders.add(new DynamicSupplyRegionBuilder());
		regionsFactory = new BuilderBasedFactory<Region>(regionBuilders);
	}

	private static JSONObject loadJSONFile(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

	private static void startBatchMode() throws Exception {
		JSONObject jo = loadJSONFile(new FileInputStream(new File(inFile)));
		OutputStream os = outFile != null ? new FileOutputStream(new File(outFile)) : System.out;
		Simulator sim = new Simulator(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"), animalsFactory, regionsFactory);
		Controller ctrl = new Controller(sim);
		
		ctrl.loadData(jo);
		ctrl.run(time, deltaTime, simpleViewer, os);
		os.close();
	}

	private static void startGUIMode() throws Exception {
		JSONObject jo = inFile != null ? loadJSONFile(new FileInputStream(new File(inFile))) : new JSONObject();
		Simulator sim = new Simulator(jo.optInt("cols", 20), jo.optInt("rows", 15), jo.optInt("width", 800), jo.optInt("height", 600), animalsFactory, regionsFactory);
		Controller ctrl = new Controller(sim);
		
		if(inFile != null) ctrl.loadData(jo);
		SwingUtilities.invokeAndWait(() -> new MainWindow(ctrl));
	}

	private static void start(String[] args) throws Exception {
		initFactories();
		parseArgs(args);
		switch(mode) {
			case BATCH: startBatchMode(); break; // -i resources/examples/ex5.json -o resources/tmp/myout.jso -t 60.0 -dt 0.03 -sv
			case GUI: startGUIMode(); break;
		}
	}

	public static void main(String[] args) {
		Utils.RAND.setSeed(2147483647l);
		try {
			start(args);
		}
		catch(Exception e) {
			ViewUtils.showErrorMsg(e.getMessage());
		}
	}
}
