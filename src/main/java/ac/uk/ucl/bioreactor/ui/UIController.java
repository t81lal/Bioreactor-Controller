package ac.uk.ucl.bioreactor.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;

import ac.uk.ucl.bioreactor.core.CommandProcessor;
import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Reactor;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
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
	private LineChart<Number, Number> tempGraph;
	@FXML
	private LineChart<Number, Number> phGraph;
	@FXML
	private LineChart<Number, Number> stirGraph;
	
	private Context context;
	private ConsoleStream consoleStream;
	private final Map<LineChart<Number, Number>, GraphTab> graphTabs;
	
	public UIController(Context context) {
		this.context = context;
		graphTabs = new HashMap<>();
	}
	
	static final int MAX_POINTS = 20;
	
	public static void setupChart(LineChart<Number, Number> chart) {
		NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(MAX_POINTS);
		xAxis.setAutoRanging(false);
		xAxis.setForceZeroInRange(true);
		
		chart.getData().add(new XYChart.Series<>());
	}
	
	public static void updateChart(LineChart<Number, Number> chart, double x, double y) {
		Platform.runLater(() -> {
			ObservableList<XYChart.Data<Number, Number>> points = chart.getData().get(0).getData();
			synchronized (points) {
				
//				if(points.size() > MAX_POINTS) {
//					points.remove(0, points.size() - MAX_POINTS);
//				}
				
				NumberAxis xAxis = (NumberAxis) chart.getXAxis();
				xAxis.setLowerBound(x - MAX_POINTS);
				xAxis.setUpperBound(x);
				points.add(new XYChart.Data<>(x, y%20));
			}
		});
	}
	
	public void initialize() {
		consoleStream = new ConsoleStream(System.out);
		System.setOut(new PrintStream(consoleStream));
		
		setupChart(tempGraph);
		
//		JFXChartUtil.setupZooming(tempGraph, (e) -> {
//			if(e.getButton() != MouseButton.PRIMARY || e.isShortcutDown()) {
//				e.consume();
//			}
//		});
		{
			ChartPanManager panner = new ChartPanManager(tempGraph);
			panner.setMouseFilter((e) -> {
				if(e.getButton() == MouseButton.SECONDARY || (e.getButton() == MouseButton.PRIMARY && e.isShortcutDown())) {
					
				} else {
					e.consume();
				}
			});
			panner.start();
		}
		
		AtomicInteger x = new AtomicInteger(0);
		AtomicInteger y = new AtomicInteger(0);
		
		boolean running = true;
		Thread t = new Thread(() -> {
			while(running) {
				if(tempGraph != null) {
					//y.set(y.get() % 20);
					float yv = Float.intBitsToFloat(y.get());
					updateChart(tempGraph, x.get(), yv);
//					if(graphTabs.containsKey(tempGraph)) {
//						updateChart(graphTabs.get(tempGraph).getOurChart(), x.get(), yv);
//					}
					x.incrementAndGet();
					y.set(Float.floatToIntBits((float)Math.sin(x.get())));
					try {
						Thread.sleep(100);
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
//			@SuppressWarnings("unchecked")
//			LineChart<Number, Number> chart = (LineChart<Number, Number>) src;
//			openGraphTabAsync(chart);
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
			context.getExecutorService().execute(() -> {
				Logging.log(Level.USER, "%s\n", text);
				context.getCommandProcessor().process(text);
			});
		}
	}
	
	private void openGraphTabAsync(LineChart<Number, Number> chart) {
		CompletableFuture.runAsync(() -> {
			GraphTab tab = findOrCreateGraphTab(chart);
			Platform.runLater(() -> {
				SelectionModel<Tab> model = tabbedPane.getSelectionModel();
				model.select(tab);
			});
		}, context.getExecutorService());
	}
	
	private GraphTab findOrCreateGraphTab(LineChart<Number, Number> chart) {
		/*for(Tab t : tabbedPane.getTabs()) {
			if(t instanceof GraphTab) {
				GraphTab gt = (GraphTab) t;
				if(gt.getParentChart() == chart) {
					return gt;
				}
			}
		}*/
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
	
	private GraphTab createGraphTab(LineChart<Number, Number> chart) {
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
