package ac.uk.ucl.bioreactor.ui;

import javafx.scene.chart.LineChart;
import javafx.scene.control.Tab;

public class GraphTab extends Tab {
	
	private final LineChart<Integer, Integer> parentChart;
	
	GraphTab(LineChart<Integer, Integer> parentChart) {
		this.parentChart = parentChart;
	}

	public LineChart<Integer, Integer> getParentChart() {
		return parentChart;
	}
}
