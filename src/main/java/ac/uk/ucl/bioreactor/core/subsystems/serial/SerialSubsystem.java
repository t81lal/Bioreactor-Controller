package ac.uk.ucl.bioreactor.core.subsystems.serial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.binding.BindManager;
import ac.uk.ucl.bioreactor.core.binding.Binding;
import ac.uk.ucl.bioreactor.core.binding.CastHelper;
import ac.uk.ucl.bioreactor.core.binding.DualDelegatingBinding;
import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.ui.NeatGraph;
import ac.uk.ucl.bioreactor.util.FileUtil;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public abstract class SerialSubsystem extends Subsystem implements SerialPortDataListener {
	private static final long DELTA_TIME_OUT = 7500;
	private static final long DELTA_RESEND = 1000;
	
	private final char subsystemId;
	private final Supplier<NeatGraph> chartSupplier;
	
	private final StringBuilder msgBuffer;

	private SerialPort port;
	private Future<?> targetSetFuture;
	private long confirmTimer;
	private long resendTimer;
	private AtomicBoolean confirmFlag;
	
	private float defaultValue;
	private float currentTarget;
	
	private float lastPlotTime;
	private float currentValue;
	
	private Binding<Float> targetBinding;
	private Binding<Float> currentBinding;
	
	private FileOutputStream dataFos;
	
	public SerialSubsystem(Context context, SubsystemDescriptor descriptor, char subsystemId, Supplier<NeatGraph> chartSupplier, int defaultTarget, String propertyName) {
		super(context, descriptor);
		this.subsystemId = subsystemId;
		this.chartSupplier = chartSupplier;
		
		try {
			File f = new File(FileUtil.outputDir, propertyName + String.valueOf(System.currentTimeMillis()) + ".csv");
			if(!f.exists()) {
				f.createNewFile();
			}
			dataFos = new FileOutputStream(f);
		} catch (IOException e) {
			Logging.logThrowable(Level.ERROR, e);
		}
		
		defaultValue = defaultTarget;
		msgBuffer = new StringBuilder();
		confirmFlag = new AtomicBoolean(false);
		
		lastPlotTime = -1;
		
		targetBinding = new DualDelegatingBinding<Float>("target" + propertyName, Float.class,
				CastHelper.fromGetter(SerialSubsystem.class, "getCurrentTarget", this, float.class),
				CastHelper.fromSetter(SerialSubsystem.class, "setTargetValue", this, float.class));
		currentBinding = new DualDelegatingBinding<>("current" + propertyName, Float.class,
				CastHelper.fromGetter(SerialSubsystem.class, "getCurrentValue", this, float.class), null);
		
		chartSupplier.get().setTargetActive(false);
		Logging.log(Level.DEBUG, "Created %s", getClass().getSimpleName());
	}
	
	public void setPort(SerialPort port) {
		if(this.port != null && this.port.isOpen()) {
			throw new UnsupportedOperationException("Already opened connection");
		}
		this.port = port;
		this.port.addDataListener(this);
	}
	
	private void registerBinds() {
		BindManager bindManager = context.getBindManager();
		bindManager.addBinding(currentBinding);
		bindManager.addBinding(targetBinding);
	}
	
	private void unregisterBinds() {
		BindManager bindManager = context.getBindManager();
		bindManager.removeBinding(currentBinding);
		bindManager.removeBinding(targetBinding);
	}

	@Override
	public void init() {
		msgBuffer.setLength(0);
		Logging.log(Level.DEBUG, "Initialised %s.", getClass().getName());
	}

	@Override
	public void start() {
		Logging.log(Level.DEBUG, "Opening port for %s...", getClass().getName());
		if(!port.openPort()) {
			stop();
			throw new IllegalStateException(String.format("Couldn't open port for %s subsystem.", getDescriptor().getName()));
		}
		registerBinds();
		setTargetValue(defaultValue);
		Logging.log(Level.DEBUG, "Done", getClass().getName());
	}
	
	@Override
	public void stop() {
		unregisterBinds();
		port.closePort();
	}
	
	@Override
	public void serialEvent(SerialPortEvent event) {
		byte[] rd = event.getReceivedData();
		//Logging.log(Level.DEBUG, "%s received %d bytes.", getDescriptor().getName(), rd.length);
		
		String s = new String(rd);
		msgBuffer.append(s);
		if(s.endsWith("\n")) {
			String msg = msgBuffer.toString();
			String[] lines = msg.split("\n");
			msgBuffer.setLength(0);
			for(String l : lines) {
				context.getExecutorService().execute(() -> _consumeMessage(l));
			}
		}
	}

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
	}
	
	protected void _consumeMessage(String msg) {
		try {
			Logging.log(Level.DEBUG, "%s Processing message: \"%s\"", getDescriptor().getName(), msg.substring(0, msg.length()-1));
			char subsystem = msg.charAt(0);
			
			if(subsystem == subsystemId) {
				char packetHeader = msg.charAt(1);
				String dataMsg = msg.substring(2);
				switch(packetHeader) {
					case 'D':
						onDataPointEvent(dataMsg);
						break;
					case 'C':
						onTargetConfirmEvent(dataMsg);
						break;
					default:
						onCustomPacketEvent(packetHeader, dataMsg);
				}
			}
		} catch(Exception e) {
			Logging.log(Level.ERROR, "Error processing message \"%s\" in %s: %s", msg, this.getClass().getName(), e.getMessage());
		}
	}
	
	private void onTargetConfirmEvent(String data) throws Exception {
		confirmFlag.set(true);
		Logging.log(Level.DEBUG, "Confirmation for %s: %s", getClass().getName(), data);
	}
	
	protected void onDataPointEvent(String data) throws Exception {
		if(!confirmFlag.get()) {
			return;
		}
		try {
			String[] parts = data.split(" ");
			currentValue = Float.parseFloat(parts[0]);
			String timeS = parts[1].substring(0, parts[1].length()-1);
			int rawTimeMs = Integer.parseInt(timeS);
			// round to closest 0.5
			double d = Math.round(rawTimeMs / 1000D * 2) / 2.0;
			if((d - lastPlotTime) >= 0.5) {
				chartSupplier.get().addPoint(d, currentValue);
				lastPlotTime = (float) d;
			}
			String logLine = rawTimeMs + "," + currentValue;
			dataFos.write(logLine.getBytes());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected abstract void onCustomPacketEvent(char header, String msg) throws Exception;
	
	public void setTargetValue(float f) {
		if(targetSetFuture == null || targetSetFuture.isDone()) {
			targetSetFuture = context.getExecutorService().submit(() -> {
				Logging.log(Level.DEBUG, "Resending target for %s", getClass().getSuperclass().getSimpleName());
				confirmTimer = resendTimer = System.currentTimeMillis();
				confirmFlag.set(false);
				sendTargetMessage(f);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
				
				while(!confirmFlag.get()) {
					long timeoutDelta = System.currentTimeMillis() - confirmTimer;
					if(timeoutDelta >= DELTA_TIME_OUT) {
						Logging.fatalError(new UnsupportedOperationException(String.format("Timeout: Device bound at %s for %s subsystem not responding to target set command",
								port.getSystemPortName(), descriptor.getName())));
					}
					long resendDelta = System.currentTimeMillis() - resendTimer;
					if(resendDelta >= DELTA_RESEND) {
						Logging.log(Level.DEBUG, "Resending target for %s", getClass().getSuperclass().getSimpleName());
						sendTargetMessage(f);
						resendTimer = System.currentTimeMillis();
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						};
					}
				}
				
				if(confirmFlag.get()) {
					this.currentTarget = f;
					chartSupplier.get().setTargetActive(true);
					chartSupplier.get().setTargetY(currentTarget);
					lastPlotTime = 0;
				}
			});
		}
	}
	
	public float getCurrentTarget() {
		return currentTarget;
	}
	
	public float getCurrentValue() {
		return currentValue;
	}
	
	private void sendTargetMessage(float f) {
		String msg = String.valueOf(subsystemId) + String.valueOf(f);
		byte[] bytes = msg.getBytes();
		port.writeBytes(bytes, bytes.length);
	}
	
	public SerialPort getPort() {
		return port;
	}
}
