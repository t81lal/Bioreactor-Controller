package ac.uk.ucl.bioreactor.core.subsystems.type;

public interface PHSubsystem {
	
	void setTargetPH(float f);

	float getTargetPH();
	
	float getCurrentPH();
}
