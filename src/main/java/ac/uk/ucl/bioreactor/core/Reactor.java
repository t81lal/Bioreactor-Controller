package ac.uk.ucl.bioreactor.core;

public interface Reactor {
	
	float getTargetTemperature();
	
	float getTargetPH();
	
	int getTargetStirringRPM();
	
	float getCurrentTemperature();
	
	float getCurrentPH();
	
	int getCurrentStirringRPM();
	
	void setTargetTemperature(float val);
	
	void setTargetPH(float val);
	
	void setTargetStirringRPM(int f);
}
