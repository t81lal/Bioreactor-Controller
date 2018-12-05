package ac.uk.ucl.bioreactor.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tab;

public class GraphTab extends Tab {

	@FXML
	private LineChart<Number, Number> ourChart;
	private final LineChart<Number, Number> parentChart;
	
	GraphTab(LineChart<Number, Number> parentChart) {
		this.parentChart = parentChart;
	}
	
	public void initialize() {
		Platform.runLater(() -> {
			UIController.setupChart(ourChart);
		});
	}
	
	public LineChart<Number, Number> getOurChart() {
		return ourChart;
	}

	public LineChart<Number, Number> getParentChart() {
		return parentChart;
	}
}
