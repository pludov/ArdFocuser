package fr.pludov.ardfocuser.driver;

public abstract class FocuserRequest {
	final String outMessage;
	
	public FocuserRequest(String message) {
		this.outMessage = message;
	}
	
	public abstract void onStarted();
	
	public abstract void onReply(String reply);
	
	public abstract void onError(String cause);

	public abstract void onCanceled(String cause);
}
