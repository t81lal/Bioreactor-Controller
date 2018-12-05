package ac.uk.ucl.bioreactor.core.subsystems.type;

public interface TemperatureSubsystem {
	
	void setTargetTemperature(float f);
	
	float getCurrentTemperature();
	
	float getTargetTemperature();
}
