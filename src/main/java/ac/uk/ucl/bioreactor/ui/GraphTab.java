package ac.uk.ucl.bioreactor.ui;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tab;

public class GraphTab extends Tab {

	@FXML
	private NeatGraph ourChart;
	private NeatGraph parentChart;
	
	GraphTab(NeatGraph parentChart) {
		this.parentChart = parentChart;
	}
	
	public void initialize() {
		parentChart.setChild(ourChart);
	}
	
	public LineChart<Number, Number> getOurChart() {
		return ourChart;
	}

	public LineChart<Number, Number> getParentChart() {
		return parentChart;
	}
}
