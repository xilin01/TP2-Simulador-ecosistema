package simulator.view;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import org.json.JSONObject;
import org.json.JSONTokener;
import simulator.control.Controller;
import simulator.launcher.Main;

@SuppressWarnings("serial")
class ControlPanel extends JPanel {
	private Controller ctrl;
	private ChangeRegionsDialog changeRegionsDialog;
	
	private JToolBar toolBar;
	private JFileChooser fileChooser;
	private boolean stopped = true;
	// Botones
	private JButton openButton;
	private JButton viewerButton;
	private JButton regionsButton;
	private JButton runButton;
	private JButton stopButton;
	private JButton quitButton;
	// Controles numéricos
	private JSpinner stepSpinner;
	private JTextField dtField;

	public ControlPanel(Controller ctrl) {
		this.ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);

		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));
		initOpenButton();
		toolBar.addSeparator();
		
		initViewerButton();
		changeRegionsDialog = new ChangeRegionsDialog(ctrl);
		initRegionsButton();
		toolBar.addSeparator();
		
		initRunButton();
		initStopButton();
		toolBar.add(new JLabel("Steps:"));
		stepSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 10000, 100));
		stepSpinner.setToolTipText("Simulation steps to run: 1-10000");
		toolBar.add(stepSpinner);
		toolBar.add(new JLabel("Delta-Time:"));
		dtField = new JTextField(Main.DEFAULT_DELTA_TIME.toString(), 5);
		dtField.setToolTipText("Real time (seconds) corresponding to a step");
		toolBar.add(dtField);
		toolBar.add(Box.createGlue()); // This aligns the quit button to the right
		toolBar.addSeparator();

		initQuitButton();
	}
	
	private void initOpenButton() {
		openButton = new JButton(new ImageIcon("resources/icons/open.png"));
		openButton.setToolTipText("Load an input file into the simulator");
		openButton.addActionListener(e -> {
			if (fileChooser.showOpenDialog(ViewUtils.getWindow(this)) == JFileChooser.APPROVE_OPTION) {
				try {
					JSONObject jo = new JSONObject(new JSONTokener((InputStream)new FileInputStream(fileChooser.getSelectedFile())));
					
					ctrl.reset(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"));
					ctrl.loadData(jo);
				}
				catch(Exception ex) {
					ViewUtils.showErrorMsg(ex.getMessage());
				}
			}
		});
		toolBar.add(openButton);
	}
	
	private void initViewerButton() {
		viewerButton = new JButton(new ImageIcon("resources/icons/viewer.png"));
		viewerButton.setToolTipText("Map Viewer");
		viewerButton.addActionListener(e -> new MapWindow(ViewUtils.getWindow(this), ctrl));
		toolBar.add(viewerButton);
	}
	
	private void initRegionsButton() {
		regionsButton = new JButton(new ImageIcon("resources/icons/regions.png"));
		regionsButton.setToolTipText("Change Regions");
		regionsButton.addActionListener(e -> changeRegionsDialog.open(ViewUtils.getWindow(this)));
		toolBar.add(regionsButton);
	}
	
	private void initRunButton() {
		runButton = new JButton(new ImageIcon("resources/icons/run.png"));
		runButton.setToolTipText("Run the simulator");
		runButton.addActionListener(e -> {
			setButtonsEnabled(false);
			stopButton.setEnabled(true);
			stopped = false;
			runSim((Integer)stepSpinner.getValue(), Double.parseDouble(dtField.getText()));
		});
		toolBar.add(runButton);
	}
	
	private void setButtonsEnabled(boolean enabled) {
		openButton.setEnabled(enabled);
		viewerButton.setEnabled(enabled);
		regionsButton.setEnabled(enabled);
		runButton.setEnabled(enabled);
		stopButton.setEnabled(enabled);
		stepSpinner.setEnabled(enabled);
		dtField.setEnabled(enabled);
		quitButton.setEnabled(enabled);
	}

	private void runSim(int n, double dt) {
		if (n > 0 && !stopped) {
			try {
				ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> runSim(n - 1, dt));
			} 
			catch (Exception e) {
				ViewUtils.showErrorMsg(e.getMessage());
				setButtonsEnabled(true);
				stopped = true;
			}
		} 
		else {
			setButtonsEnabled(true);
			stopped = true;
		}
	}
	
	private void initStopButton() {
		stopButton = new JButton(new ImageIcon("resources/icons/stop.png"));
		stopButton.setToolTipText("Stop the simulator");
		stopButton.addActionListener(e -> stopped = true);
		toolBar.add(stopButton);
	}
	
	private void initQuitButton() {
		quitButton = new JButton(new ImageIcon("resources/icons/exit.png"));
		quitButton.setToolTipText("Exit");
		quitButton.addActionListener(e -> ViewUtils.quit(this));
		toolBar.add(quitButton);
	}
}
