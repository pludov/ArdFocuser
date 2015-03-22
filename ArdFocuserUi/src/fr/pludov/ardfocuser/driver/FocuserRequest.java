package fr.pludov.ardfocuser.driver;

public abstract class FocuserRequest {
	final String outMessage;
	
	public FocuserRequest(String message) {
		this.outMessage = message;
	}
	
	abstract void onStarted();
	
	abstract void onReply(String reply);
	
	abstract void onError(String cause);

	abstract void onCanceled(String cause);
}
