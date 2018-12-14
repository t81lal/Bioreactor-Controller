package ac.uk.ucl.bioreactor.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class UIController {
	
	@FXML
	private Button sendButton;
	@FXML
	private TextArea consoleTextArea;
	@FXML
	private TextField commandTextField;
	@FXML
	private CheckBox scrollCheckbox;
	@FXML
	private TabPane tabbedPane;

	@FXML
	private NeatGraph tempGraph;
	@FXML
	private NeatGraph phGraph;
	@FXML
	private NeatGraph stirGraph;
	
	private Context context;
	private ConsoleStream consoleStream;
	private final Map<NeatGraph, GraphTab> graphTabs;
	
	public UIController(Context context) {
		this.context = context;
		graphTabs = new HashMap<>();
	}
	
	public NeatGraph getTempGraph() {
		return tempGraph;
	}
	
	public NeatGraph getPHGraph() {
		return phGraph;
	}
	
	public NeatGraph getStirGraph() {
		return stirGraph;
	}
	
	public void initialize() {
		consoleStream = new ConsoleStream(System.out);
		System.setOut(new PrintStream(consoleStream));
		
		commandTextField.setText("");
	}
	
	public void onGraphFullTab(MouseEvent e) {
		Object src = e.getSource();
		if(src instanceof NeatGraph) {
			NeatGraph g = (NeatGraph) src;
			openGraphTabAsync(g);
		}
	}
	
	public void onScrollCheckboxPress(ActionEvent e) {
		if(e.getSource() == scrollCheckbox) {
			ScrollBar scrollBar = (ScrollBar) consoleTextArea.lookup(".scroll-bar:vertical");
			scrollBar.setDisable(scrollCheckbox.isSelected());
		}
	}
	
	public void processCommand(ActionEvent e) {
		if(e.getSource() == sendButton || e.getSource() == commandTextField) {
			String text = commandTextField.getText();
			commandTextField.clear();
			context.getExecutorService().execute(() -> {
				Logging.log(Level.USER, "%s", text);
				context.getCommandProcessor().process(text);
			});
		}
	}
	
	private void openGraphTabAsync(NeatGraph chart) {
		CompletableFuture.runAsync(() -> {
			GraphTab tab = findOrCreateGraphTab(chart);
			Platform.runLater(() -> {
				SelectionModel<Tab> model = tabbedPane.getSelectionModel();
				if(!tabbedPane.getTabs().contains(tab)) {
					tabbedPane.getTabs().add(tab);
				}
				model.select(tab);
			});
		}, context.getExecutorService());
	}
	
	private GraphTab findOrCreateGraphTab(NeatGraph chart) {
		if(graphTabs.containsKey(chart)) {
			return graphTabs.get(chart);
		} else {
			GraphTab tab = createGraphTab(chart);
			Platform.runLater(() -> {
				tabbedPane.getTabs().add(tab);
				graphTabs.put(chart, tab);
			});
			return tab;
		}
	}
	
	private GraphTab createGraphTab(NeatGraph chart) {
		GraphTab tab = new GraphTab(chart);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/graphtab.fxml"));
		loader.setController(tab);
		loader.setRoot(tab);
		try {
			loader.load();
			tab.setText(chart.getTitle() + " Graph");
			tab.bind();
			return tab;
		} catch (IOException e) {
			Logging.fatalError("Could not load graph tab fxml", e);
			return null;
		}
	}
	
	private class ConsoleStream extends OutputStream {
		private final OutputStream parentStream;
		ConsoleStream(OutputStream parentStream) {
			this.parentStream = parentStream;
		}

		@Override
		public void write(int b) throws IOException {
			if(parentStream != null) {
				parentStream.write(b);
			}
			if(consoleTextArea != null) {
				String text = String.valueOf((char)b);
				Platform.runLater(() -> consoleTextArea.appendText(text));
			}
		}
	}
}
