package fr.pludov.ardfocuser.driver;

public enum FilterWheelMotorStatus {
	Idle('I'),
	Moving('M'),
	MovingCalibration('C'),
	FailedCalibration('K');
	
	final char protocolValue;
	
	private FilterWheelMotorStatus(char c)
	{
		this.protocolValue = c;
	}

	public static FilterWheelMotorStatus fromProtocol(char motorStateC) {
		for(FilterWheelMotorStatus fwms : values()) {
			if (fwms.protocolValue == motorStateC) {
				return fwms;
			}
		}
		return null;
	}
}
