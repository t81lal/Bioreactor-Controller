package ac.uk.ucl.bioreactor.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.glass.ui.PlatformFactory;

import ac.uk.ucl.bioreactor.core.CommandProcessor;
import ac.uk.ucl.bioreactor.core.Logging;
import ac.uk.ucl.bioreactor.core.Reactor;
import ac.uk.ucl.bioreactor.core.Logging.Level;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart.Data;
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
	private LineChart<Float, Float> tempGraph;
	@FXML
	private LineChart<Integer, Integer> phGraph;
	@FXML
	private LineChart<Integer, Integer> stirGraph;
	
	private ConsoleStream consoleStream;
	private final ExecutorService executorService;
	private CommandProcessor commandProcessor;
	
	public UIController(Reactor reactor) {
		executorService = Executors.newCachedThreadPool();
		commandProcessor = new CommandProcessor(executorService, reactor);
	}
	
	public void initialize() {
		consoleStream = new ConsoleStream(System.out);
		System.setOut(new PrintStream(consoleStream));
		
		XYChart.Series<Float, Float> series = new XYChart.Series<>();
		tempGraph.getData().add(series);
		((NumberAxis) (Axis)tempGraph.getXAxis()).setForceZeroInRange(false);
		
		AtomicInteger x = new AtomicInteger(0);
		boolean running = true;
		Thread t = new Thread(() -> {
			while(running) {
				if(tempGraph != null) {
					Platform.runLater(() -> {
						synchronized (series.getData()) {
							series.getData().add(new XYChart.Data<Float, Float>((float)x.get(), (float)Math.random() * 50));
							
							if(x.get() < 50) {
							} else {
								series.getData().remove(0);
							}
							x.incrementAndGet();
						}
					});

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}
	
	public void onGraphFullTab(MouseEvent e) {
		Object src = e.getSource();
		if(src instanceof LineChart) {
			@SuppressWarnings("unchecked")
			LineChart<Integer, Integer> chart = (LineChart<Integer, Integer>) src;
			openGraphTabAsync(chart);
		}
	}
	
	public void onScrollCheckboxPress(ActionEvent e) {
		if(e.getSource() == scrollCheckbox) {
			ScrollBar scrollBar = (ScrollBar) consoleTextArea.lookup(".scroll-bar:vertical");
			scrollBar.setDisable(!scrollCheckbox.isSelected());
		}
	}
	
	public void processCommand(ActionEvent e) {
		if(e.getSource() == sendButton || e.getSource() == commandTextField) {
			String text = commandTextField.getText();
			commandTextField.clear();
			executorService.execute(() -> {
				Logging.log(Level.USER, "%s\n", text);
				commandProcessor.process(text);
			});
		}
	}
	
	private void openGraphTabAsync(LineChart<Integer, Integer> chart) {
		CompletableFuture.runAsync(() -> {
			GraphTab tab = findOrCreateGraphTab(chart);
			Platform.runLater(() -> {
				SelectionModel<Tab> model = tabbedPane.getSelectionModel();
				model.select(tab);
			});
		}, executorService);
	}
	
	private GraphTab findOrCreateGraphTab(LineChart<Integer, Integer> chart) {
		for(Tab t : tabbedPane.getTabs()) {
			if(t instanceof GraphTab) {
				GraphTab gt = (GraphTab) t;
				if(gt.getParentChart() == chart) {
					return gt;
				}
			}
		}
		GraphTab tab = createGraphTab(chart);
		Platform.runLater(() -> tabbedPane.getTabs().add(tab));
		return tab;
	}
	
	private GraphTab createGraphTab(LineChart<Integer, Integer> chart) {
		GraphTab tab = new GraphTab(chart);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/graphtab.fxml"));
		loader.setController(tab);
		loader.setRoot(tab);
		try {
			loader.load();
			tab.setText(chart.getTitle() + " Graph");
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
