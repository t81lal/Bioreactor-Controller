package ac.uk.ucl.bioreactor.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gillius.jfxutils.EventHandlerManager;
import org.gillius.jfxutils.chart.ChartPanManager;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class NeatGraph extends LineChart<Number, Number> {
	
	private final ChartPanManager panner;
	
	private double scaleDelta = 1.1;
	private int maxSpan = 20;
	
	private AtomicBoolean isUserControlling;
	
	private final Series<Number, Number> valueSeries;
	private final Series<Number, Number> targetSeries;

	private boolean isTargetActive;
	private double targetY;
	
	private NeatGraph child;
	
	public NeatGraph() {
		this(new NumberAxis(), new NumberAxis());
	}
	
	private NeatGraph(NumberAxis xAxis, NumberAxis yAxis) {
		super(xAxis, yAxis);
		
		xAxis.setTickUnit(1);
		
		isUserControlling = new AtomicBoolean(false);
		
		valueSeries = new Series<>();
		valueSeries.setName("Data");
		targetSeries = new Series<>();
		targetSeries.setName("Target");
		
		valueSeries.getData().addListener(new ListChangeListener<Data<Number, Number>>() {
			@Override
			public void onChanged(Change<? extends Data<Number, Number>> c) {
				double largestX = 0;
				while(c.next()) {
					if(c.wasAdded()) {
						List<? extends Data<Number,Number>> added = c.getAddedSubList();
						for(Data<Number, Number> d : added) {
							largestX = Math.max(largestX, d.getXValue().doubleValue());
						}
					}
				}
				
				if(!isUserControlling.get()) {
					NumberAxis xAxis = (NumberAxis) getXAxis();
					xAxis.setLowerBound(Math.max(0, largestX - maxSpan));
					xAxis.setUpperBound(largestX);
				}
				
				updateTargetSeries();
			}
		});
		
		EventHandlerManager handlerManager = new EventHandlerManager(this);
		handlerManager.addEventHandler(true, MouseEvent.MOUSE_RELEASED, (e) -> {
			isUserControlling.set(false);
		});
		panner = new ChartPanManager(this);
		panner.setMouseFilter((e) -> {
			if(e.getButton() == MouseButton.SECONDARY || (e.getButton() == MouseButton.PRIMARY && e.isShortcutDown())) {
				isUserControlling.set(true);
			} else {
				e.consume();
			}
		});
		panner.start();

		getData().add(valueSeries);
		//getData().add(targetSeries);
		
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(maxSpan);
		xAxis.setAutoRanging(false);
		xAxis.setForceZeroInRange(true);
		
		setOnScroll((e) -> {
			e.consume();
			if(e.getDeltaY() != 0) {
				double sf = e.getDeltaY() > 0 ? scaleDelta : 1/scaleDelta;
				setScaleX(getScaleX() * sf);
				setScaleY(getScaleY() * sf);
			}
		});
		
		setOnMousePressed((e) -> {
			if(e.getClickCount() == 2) {
				setScaleX(1);
				setScaleY(1);
			}
		});
		
		initialiseTargetSeries();
		setTargetActive(false);
	}
	
	public void setChild(NeatGraph child) {
		if(child == this) {
			throw new IllegalArgumentException();
		}
		this.child = child;
		this.child.setTargetActive(isTargetActive);
		this.child.setTargetY(targetY);
	}
	
	public void addPoint(double x, double y) {
		Platform.runLater(() -> valueSeries.getData().add(new Data<>(x, y)));
		
		if(child != null) {
			child.addPoint(x, y);
		}
	}
	
	private void initialiseTargetSeries() {
		ObservableList<Data<Number, Number>> data = targetSeries.getData();
		data.add(new Data<>(0, 0));
		data.add(new Data<>(maxSpan * 1.5, 0));
	}
	
	public void setTargetActive(boolean isTargetActive) {
		if(this.isTargetActive != isTargetActive) {
			this.isTargetActive = isTargetActive;
			
			Platform.runLater(() -> {
				if(isTargetActive) {
					getData().add(targetSeries);
				} else {
					getData().remove(targetSeries);
				}
			});
		}
		
		if(child != null) {
			child.setTargetActive(isTargetActive);
		}
	}
	
	public void setTargetY(double targetY) {
		setTargetActive(true);
		this.targetY = targetY;
		Platform.runLater(() -> {
			targetSeries.setName("Target(" + String.valueOf(targetY) + ")");
			ObservableList<Data<Number, Number>> data = targetSeries.getData();
			synchronized (data) {
				for(Data<Number, Number> d : data) {
					d.setYValue(targetY);
				}
			}
		});
		
		if(child != null) {
			child.setTargetY(targetY);
		}
	}
	
	private void updateTargetSeries() {
		Platform.runLater(() -> {
			ObservableList<Data<Number, Number>> data = targetSeries.getData();
			synchronized (data) {
				Data<Number, Number> p = data.get(1);
				double upper = ((NumberAxis)getXAxis()).getUpperBound();
				p.setXValue(upper*1.5);
			}
		});
	}
}
