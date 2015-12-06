package fr.pludov.ardfocuser.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.SwingUtilities;

import fr.pludov.ardfocus.utils.WeakListenerOwner;

public class Client {

	// Les commande commencent et se termine par:
	static final char CommandSeparator = '#';
	
	static final char SetTargetCommand = 'T';
	static final char GetTempCommand = 'C';
//	static final char SetInitialPositionCommand = 'I'; // C'est pas possible en ascom ça je crois
	static final char GetCurrentPositionCommand = 'P';
	static final char HaltCommand = 'H';
	static final char IsMotorMovingCommand = 'M';
	static final char GetVersionCommand = 'V';
	static final char GetRangeCommand = 'R';
	
	static final char GetFilters = 'N';
	static final char GetFilterPos = 'F';
	static final char ActivateFilter = 'A';
	
	
	int clientId;
	Focuser focuser;
	Socket socket;
	// Clos par le thread swing
	InputStream is;
	// Clos par le worker
	OutputStream os;
	Thread consumer;
	
	boolean statusReceivedSincePreviousOrder;

	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	
	public Client(Focuser parent, Socket s) throws IOException {
		this.focuser = parent;
		this.socket = s;
		
		try {
			this.clientId = s.getPort();
			this.is = s.getInputStream();
			this.os = s.getOutputStream();
		} catch(IOException e) {
			s.close();
			throw e;
		}
		
		this.focuser.listeners.addListener(this.listenerOwner , new IFocuserListener() {
			
			@Override
			public void statusChanged() {
			}
			
			@Override
			public void parametersChanged() {
				statusReceivedSincePreviousOrder = true;
			}
			
			@Override
			public void filterDefinitionChanged() {
			}
			
			@Override
			public void broadcastError(String string) {
			}
		});
		
		consumer = new Thread("Processor for " + this.toString()) {
			@Override
			public void run() {
				asyncRead();
			}
		};
		consumer.start();
	}
	
	void asyncNotifyDeath()
	{
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (focuser != null) {
						focuser.clients.remove(this);
					}
					disconnectFromFocuser();
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class ClientRequest {
		boolean done;
		String result;
		
		synchronized void setDone() {
			this.done = true;
			this.notifyAll();
		}
		
		synchronized void setError() {
			if (done) return;
		
			result = "ERR#";
			setDone();
		}
		void waitDone()
		{
			synchronized(this) {
				while(!this.done) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	abstract class WaitStatusUpdate extends ClientRequest {
		
		private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);

		// Appellé depuis swing
		void start()
		{
			if ((!statusReceivedSincePreviousOrder) || (!focuser.parametersReceived())) {
				// Il faut d'abord attendre un nouveau status
				focuser.listeners.addListener(this.listenerOwner, new IFocuserListener() {
					
					@Override
					public void statusChanged() {
						setError();
					}

					@Override
					public void parametersChanged() {
						if (focuser == null) {
							setError();
						} else {
							onStatusReady();
							setDone();
						}
					}

					@Override
					public void filterDefinitionChanged() {
					}
					
					@Override
					public void broadcastError(String string) {
					}
				});
			} else {
				onStatusReady();
				setDone();
			}
		}
		
		// Appellé depuis swing
		abstract void onStatusReady();
		
	}
	
	abstract class FocuserRequestFromClient extends ClientRequest
	{
		final FocuserRequest request;
	
		FocuserRequestFromClient(String focuserCommand) {
			request = new FocuserRequest(focuserCommand) {
				@Override
				public void onStarted() {
					System.out.println(Client.this.toString() + ": starting command " + outMessage);
				}
				
				@Override
				public void onReply(String reply) {
					System.out.println(Client.this.toString() + ": Got reply for command " + outMessage + " : " + reply);
					FocuserRequestFromClient.this.onReply(reply);
				}
				
				@Override
				public void onError(String cause) {
					System.out.println(Client.this.toString() + ": Got error for command " + outMessage);
					setError();
				}
				
				@Override
				public void onCanceled(String cause) {
					System.out.println(Client.this.toString() + ": Got canceled for command " + outMessage);
					setError();
				}
			};
			
			System.out.println(Client.this.toString() + ": queuing command " + focuserCommand);

			focuser.queueRequest(request, false);
		}
		
		abstract void onReply(String reply);
	}
	
	// Démarre le début d'une requete.
	ClientRequest handleCommand(String command) {
		System.out.println("From client " + this + ": " + command);
		if (focuser == null) {
			ClientRequest result = new ClientRequest();
			result.setError();
			return result;
		}
		
		command = command.substring(0, command.length() - 1);
		
		if (command.length() == 0) {
			ClientRequest response = new ClientRequest();
			response.result = "OK!#";
			response.setDone();
			return response;
		}
		
		switch(command.charAt(0)) {
			case GetCurrentPositionCommand:
			{
				WaitStatusUpdate response = new WaitStatusUpdate() {
					@Override
					void onStatusReady() {
						result = "" + GetCurrentPositionCommand +  focuser.getMotorPosition() + ":OK#";
					}
				};
				response.start();
				return response;
			}
			
			case IsMotorMovingCommand:
			{
				WaitStatusUpdate response = new WaitStatusUpdate() {
					@Override
					void onStatusReady() {
						result = "" + IsMotorMovingCommand +  (focuser.getMotorState() ? 1 : 0) + ":OK#";
					}
				};
				response.start();
				return response;
			}
			
			case GetTempCommand:
			{
				// FIXME: il faudrait au moins qu'on ait reçu une fois l'info du focuser !
				ClientRequest response = new ClientRequest();
				Double scopeTemp = focuser.getExtTemp();
				int is = (scopeTemp == null) ? 10000 : (int)(scopeTemp * 100);
				response.result = "" + GetTempCommand + is + ":OK#";
				response.setDone();
				return response;
			}
			case SetTargetCommand:
			{
				final String focuserCommand = command;
				FocuserRequestFromClient request = new FocuserRequestFromClient(focuserCommand) {
					@Override
					void onReply(String reply) {
						result = reply + "#";
						statusReceivedSincePreviousOrder = false;
						setDone();
					}
				};
				return request;
			}

			case ActivateFilter:
			{
				int pos;
				
				try {
					int filter = Integer.parseInt(command.substring(1));
					if (filter < 0 || filter >= focuser.getFilterDefinitions().size()) throw new Exception("invalid filter id");
					pos = focuser.getFilterDefinitions().get(filter).getPosition();
				} catch(Exception e) {
					ClientRequest result = new ClientRequest();
					result.setError();
					return result;
				}
				// FIXME: c'est bien F ?
				FocuserRequestFromClient request = new FocuserRequestFromClient("F" + pos) {
					@Override
					void onReply(String reply) {
						result = reply + "#";
						statusReceivedSincePreviousOrder = false;
						setDone();
					}
				};
				return request;
			}
			case GetFilterPos:
			{
				WaitStatusUpdate response = new WaitStatusUpdate() {
					@Override
					void onStatusReady() {
						int filter;

						if (focuser.getFilterWheelState() == null) {
							filter = -1;
						} else {
							switch(focuser.getFilterWheelState()) {
							case FailedCalibration:
							case MovingCalibration:
							case Moving:
								filter = -1;
								break;
							case Idle:
								filter = -1;
								for(int i = 0; i < focuser.getFilterDefinitions().size(); ++i) {
									if (focuser.getFilterWheelPosition() == focuser.getFilterDefinitions().get(i).getPosition()) {
										filter = i;
										break;
									}
								}
								break;
							default:
								throw new RuntimeException("internal error");
							
							}
						}
						
						result = "" + GetFilterPos +  filter + ":OK#";
					}
				};
				response.start();
				return response;
			}
			

			case HaltCommand:
			{
				final String focuserCommand = command;
				FocuserRequestFromClient request = new FocuserRequestFromClient(focuserCommand) {
					@Override
					void onReply(String reply) {
						result = reply + "#";
						statusReceivedSincePreviousOrder = false;
						setDone();
					}
				};
				return request;
			}

			case GetVersionCommand:
			{
				ClientRequest response = new ClientRequest();
				response.result = "V1.0#";
				response.setDone();
				return response;
			}
			
			case GetRangeCommand:
			{
				ClientRequest response = new ClientRequest();
				
				response.result = "R" + focuser.getMaxMotorPosition() + "#";
				response.setDone();
				return response;
			}
			
			default:
			{
				System.out.println("wrong command: " + command);
				ClientRequest fallback = new ClientRequest();
				fallback.setError();
				return fallback;
			}
			
			
		}
		
	}
	
	String asyncHandleCommand(final String command) throws IOException
	{
		final ClientRequest [] request = new ClientRequest[1];
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					request[0] = handleCommand(command);
				}
			});
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			throw new IOException("Command Failed : " + command);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IOException("Command Failed : " + command);
		}
		
		request[0].waitDone();
		System.out.println("To client " + this + ": " + request[0].result);
		return request[0].result;
	}
	
	void asyncRead()
	{
		try {
			while(true) {
				StringBuilder builder = new StringBuilder();
				while(builder.length() < 1 || builder.charAt(builder.length() - 1) != '#') {
					// Lire une commande
					int c = is.read();
					if (c == -1) {
						asyncNotifyDeath();
						return;
					}
					builder.append((char)c);
				}
				
				// On a une commande
				String result = asyncHandleCommand(builder.toString());
				
				os.write(result.getBytes(StandardCharsets.ISO_8859_1));
			}
		} catch(IOException e) {
			e.printStackTrace();
			asyncNotifyDeath();
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString() {
		return "Client #" + clientId;
	}

	/** Appellé quand le focuser se déconnecte (doit déconnecter le socket correspondant */
	public void disconnectFromFocuser() {
		focuser = null;
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
