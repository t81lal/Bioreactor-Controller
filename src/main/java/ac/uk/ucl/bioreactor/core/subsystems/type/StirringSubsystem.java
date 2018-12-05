package ac.uk.ucl.bioreactor.core.subsystems.type;

public interface StirringSubsystem {

	void setTargetStirringRate(float f);
	
	float getTargetStirringRate();
	
	float getCurrentStirringRate();
}
