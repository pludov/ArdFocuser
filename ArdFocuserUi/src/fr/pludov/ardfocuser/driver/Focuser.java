package fr.pludov.ardfocuser.driver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jssc.SerialPort;
import jssc.SerialPortException;
import fr.pludov.ardfocus.utils.WeakListenerCollection;
import fr.pludov.ardfocus.utils.WeakListenerOwner;
import fr.pludov.ardfocuser.driver.ThreadedServerSocket.ISocketServerListener;

public class Focuser {
	static final char prefix = '«';
	static final char suffix = '»';
	
	static final char commandHello = 'X';
	
	SerialPort serialPort;
	
	SerialReader readThread;
	SerialWriter writeThread;
	
	String handShake;
	Timer handShakeTimer;
	int handShakeFailedCount;
	
	
	/** Status au niveau communication */
	FocuserStatus currentStatus;

	ThreadedServerSocket serverSocket;
	
	private final WeakListenerOwner listenerOwner = new WeakListenerOwner(this);
	public final WeakListenerCollection<IFocuserListener> listeners;

	private final List<FocuserRequest> pendingRequests;
	private FocuserRequest currentRequest;

	final Set<Client> clients;
	
	/** -1 while uninitialized */
	int motorPosition;
	/** -1 while uninitialized; 0 - 1 otherwise */
	int motorState;
	/** null while uninitialized or N/A */
	Double scopeTemp;
	/** null while uninitialized or N/A */
	Double extTemp;
	/** null while uninitialized or N/A */
	Double extHum;
	/** null while uninitialized or N/A */
	Double battery;
	/** null while uninitialized or N/A */
	Double heater;
	
	
	public Focuser() {
		this.currentStatus = FocuserStatus.Disconnected;
		this.listeners = new WeakListenerCollection<>(IFocuserListener.class, true);
		this.pendingRequests = new LinkedList<>();
		this.currentRequest = null;
		this.clients = new HashSet<>();
		clearParameters();
	}

	void clearParameters()
	{
		motorPosition = -1;
		motorState = -1;
		scopeTemp = null;
		extTemp = null;
		extHum = null;
		battery = null;
		heater = null;
	}
	
	public FocuserStatus getCurrentStatus()
	{
		return currentStatus;
	}

	private void internalClose()
	{
		abortHandShake();
		
		if (this.serverSocket != null) {
			// On ferme !
			this.serverSocket.close();
			this.serverSocket = null;
		}
		for(Client c : clients) {
			c.disconnectFromFocuser();
		}
		clients.clear();
		
		
		if (writeThread != null) {
			writeThread.close();
			writeThread = null;
		}
		if (this.serialPort != null) {
			Logger.log("Closing");
			
			final SerialPort toClose = this.serialPort;
			this.serialPort = null;
			try {
				toClose.closePort();
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void discardRequests(String cause)
	{
		List<FocuserRequest> toDiscard = new ArrayList<>();
		toDiscard.addAll(this.pendingRequests);
		this.pendingRequests.clear();
		
		FocuserRequest oldCurrent = currentRequest;
		currentRequest = null;
		
		if (oldCurrent != null) {
			oldCurrent.onError(cause);
		}

		for(FocuserRequest td : toDiscard) {
			td.onCanceled(cause);
		}
	}
	
	public void close()
	{
		internalClose();
		switchCurrentStatus(FocuserStatus.Disconnected);
		discardRequests("closed by user");
	}
	
	private void switchCurrentStatus(FocuserStatus target)
	{
		if (this.currentStatus == target) {
			return;
		}
		// Dans tous les cas, on perd l'état courant
		clearParameters();
		this.currentStatus = target;
		listeners.getTarget().statusChanged();
	}
	
	public void use(SerialPort commPort) {
		internalClose();
		switchCurrentStatus(FocuserStatus.Disconnected);
		discardRequests("closed by user");
		
		Logger.reset();
		switchCurrentStatus(FocuserStatus.InitialHandshake);
		try {
			if (!commPort.openPort()) {
				throw new Exception("Failed to open port");
			}
			serialPort = commPort;
			if (!serialPort.setParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)) {
				throw new Exception("Failed to set port speeds");
			}
			if (!serialPort.setDTR(false)) {
				throw new Exception("Failed to set port DTR");
			}
			
			readThread = new SerialReader(serialPort, this);
			writeThread = new SerialWriter(serialPort, this);

			readThread.start();
			writeThread.start();
			
			handShakeFailedCount = 0;
			restartHandShake();
			
			
			// FIXME: mettre un timeout.
			
			// writeThread.push(prefix + "S1120008000700080050020" + suffix);
		} catch(Throwable t) {
			switchCurrentStatus(FocuserStatus.Disconnected);
			t.printStackTrace();
			listeners.getTarget().broadcastError("Erreur d'ouverture du port: " + t.getMessage());
			internalClose();
		}
	}
	
	private void abortHandShake()
	{
		if (handShakeTimer != null) {
			handShakeTimer.stop();
			handShakeTimer = null;
		}
		handShake = null;
	}
	
	private void restartHandShake()
	{
		abortHandShake();
		Logger.log("Trying to connect");
		handShake = commandHello + randomString((char)33, (char)127, 8);
		writeThread.push(prefix + handShake + suffix);
		handShakeTimer = new Timer(500, null);
		handShakeTimer.setRepeats(false);
		handShakeTimer.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int handShakeFailedMaxCount = 15;
				handShakeFailedCount++;
				Logger.log("Handshake failed");
				System.out.println("handShake failed for " + handShake + "(" + handShakeFailedCount + "/" + handShakeFailedMaxCount + ")");
				if (handShakeFailedCount >= handShakeFailedMaxCount) {
					switchCurrentStatus(FocuserStatus.Disconnected);
					listeners.getTarget().broadcastError("Erreur d'initialisation du protocole");
					internalClose();
				} else {
					restartHandShake();
				}
			}
		});
		handShakeTimer.start();
	}
	
	private static String randomString(char min, char max, int count)
	{
		StringBuffer result = new StringBuffer();
		for(int i = 0; i < count; ++i)
		{
			int c = (int)Math.floor(min + Math.random() * (max - min + 1));
			if (c > max) {
				throw new RuntimeException("marche pas.");
			}
			result.append((char)c);
		}
		return result.toString();
	}

	private static Double decodeTemp(String buffer)
	{
		if (buffer.trim().equals("")) {
			return null;
		}
		int scaled = Integer.parseInt(buffer, 16);
		
		return (scaled - 32768) / 100.0;
	}
	
	private static Double decodeHum(String buffer)
	{
		if (buffer.trim().equals("")) {
			return null;
		}
		int scaled = Integer.parseInt(buffer, 16);
		
		return scaled / 40.95;
	}

	private static Double decodeVolt(String buffer)
	{
		if (buffer.trim().equals("")) {
			return null;
		}
		int scaled = Integer.parseInt(buffer, 16);
		
		return scaled / 100.0;
	}

	private static Double decodePct(String buffer)
	{
		if (buffer.trim().equals("")) {
			return null;
		}
		int scaled = Integer.parseInt(buffer, 16);
		
		return scaled / 2.550;
	}
	
	private void decodeState(String status)
	{
		status = status.substring(1);
		if (status.length() != 22) {
			closeWithError("Wrong status encoding");
		}
		String motor = status.substring(0, 0 + 5);
		this.motorPosition = Integer.parseInt(motor, 16);
		String motorState = status.substring(5, 6);
		this.motorState = Integer.parseInt(motorState, 2);
		this.scopeTemp = decodeTemp(status.substring(6, 6 + 4));
		this.extTemp = decodeTemp(status.substring(10, 10 + 4));
		this.extHum = decodeHum(status.substring(14, 14 + 3));
		this.battery = decodeVolt(status.substring(17, 17 + 3));
		this.heater = decodePct(status.substring(20, 20 + 2));
		
		Logger.stateUpdated(this);
		
		listeners.getTarget().parametersChanged();
	}
	
	private void closeWithError(String errorMessage)
	{
		Logger.log("Error: " + errorMessage);

		internalClose();
		switchCurrentStatus(FocuserStatus.Disconnected);
		listeners.getTarget().broadcastError(errorMessage);
		discardRequests(errorMessage);
	}
	
	void onMessageReceived(String message)
	{
		System.out.println("Protocol message: " + message);
		if (this.currentStatus == FocuserStatus.InitialHandshake) {
			if (message.equals(handShake)) {
				Logger.log("Connection established");
				System.out.println("handShake succeeded");
				abortHandShake();
				switchCurrentStatus(FocuserStatus.Connected);
				int port = 1051;
				try {
					this.serverSocket = new ThreadedServerSocket(port);
					this.serverSocket.listeners.addListener(this.listenerOwner, new ISocketServerListener() {
						
						@Override
						public void serverDead() {
							serverSocket = null;
							listeners.getTarget().broadcastError("Local server died");
						}
						
						@Override
						public void newConnection(Socket socket) {
							Client c;
							try {
								c = new Client(Focuser.this, socket);
							} catch (IOException e) {
								listeners.getTarget().broadcastError("Failed to accept connection : " + e.getLocalizedMessage());
								return;
							}
							Focuser.this.clients.add(c);
						}
					});
				} catch(Exception e) {
					e.printStackTrace();
					listeners.getTarget().broadcastError("Failed to bind port " + port + " : " + e.getLocalizedMessage());
					this.serverSocket = null;
				}
			}
		} else {
			if (message.startsWith("S")) {
				// Message de status
				decodeState(message);
			} else {
				Logger.log("Got reply: " + message);
				if (currentRequest != null) {
					FocuserRequest currentRequest = this.currentRequest;
					this.currentRequest = null;
					currentRequest.onReply(message);
					startAnyRequest();
				} else {
					closeWithError("protocol error");
				}
			}
		}
	}

	/** Peut être appellé depuis n'importe quel thread. Les evenements seront gérés dans le thread swing 
	 * @param discardPending TODO*/
	public void queueRequest(final FocuserRequest request, final boolean discardPending)
	{
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				if (currentStatus != FocuserStatus.Connected) {
					request.onError("Not connected");
				} else {
					if (discardPending) {
						pendingRequests.clear();
					}
					pendingRequests.add(request);
					startAnyRequest();
				}
			}
		});
	}
	
	void startAnyRequest()
	{
		if (this.currentRequest != null) return;
		if (this.currentStatus != FocuserStatus.Connected) return;
		if (this.pendingRequests.isEmpty()) return;
		this.currentRequest = this.pendingRequests.remove(0);
		Logger.log("Request: " + this.currentRequest.outMessage);
		this.writeThread.push(prefix + this.currentRequest.outMessage + suffix);
		this.currentRequest.onStarted();
	}
	
	void ioError(final SerialPort from, final String message)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (serialPort == from) {
					closeWithError(message);
				}
			}			
		});
	}
	
	static class SerialReader extends Thread
	{
		Focuser focuser;
		SerialPort inputStream;
		StringBuffer garbage = new StringBuffer();
		
		SerialReader(SerialPort serialPort, Focuser focuser)
		{
			setName("Serial read thread");
			this.inputStream = serialPort;
			this.focuser = focuser;
		}
		
		@Override
		public void run() {
			StringBuilder sb = new StringBuilder();
			while(true)
			{
				char c;
				try {
					byte[] b = inputStream.readBytes(1);
					c = (char)(((int)b[0]) & 0xff);
					if (c == 0) {
						continue;
					}
				} catch (SerialPortException e) {
					e.printStackTrace();
					focuser.ioError(this.inputStream, "Reader: " + e.getMessage());
					return;
				}
				if (sb.length() == 0) {
					if (c != prefix) {
						garbage.append(c);
						if (c == '\n') {
							emptyTrash();
						}
						continue;
					}
					emptyTrash();
				}
				sb.append(c);
				if (c == suffix) {
					final String message = sb.toString();
					sb = new StringBuilder();
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (focuser.serialPort != inputStream) {
								return;
							}
							focuser.onMessageReceived(message.substring(1, message.length() - 1));
						}
					});
				}
				
			}
		}

		private void emptyTrash() {
			if (garbage.length() > 0) {
				System.out.print("readed: ");
				if (garbage.charAt(garbage.length() - 1) == '\n') {					
					System.out.print(garbage.toString());
				} else {
					System.out.println(garbage.toString());
				}
				garbage.setLength(0);
			}
			
		}
	}
	
	static class SerialWriter extends Thread
	{
		Focuser focuser;
		SerialPort outputStream;
		
		List<String> sending;
		
		SerialWriter(SerialPort outputStream, Focuser focuser)
		{
			setName("Serial write thread");
			this.outputStream = outputStream;
			this.focuser = focuser;
			
			sending = new LinkedList<>();
		}
		
		synchronized void push(String message)
		{
			sending.add(message);
			notifyAll();
		}
		
		synchronized void close()
		{
			sending = null;
			notifyAll();
		}
		
		@Override
		public void run() {
			while(true) {
				String toSend;
				synchronized(this) {
					while(sending != null && sending.isEmpty()) {
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}
					if (sending == null) return;
					toSend = sending.remove(0);
				}
				try {
					if (!outputStream.writeBytes(toSend.getBytes(StandardCharsets.ISO_8859_1))) {
						focuser.ioError(this.outputStream, "writeBytes returned false");
						return;
					}
				} catch (SerialPortException e) {
					e.printStackTrace();
					focuser.ioError(this.outputStream, "Writer: " + e.getMessage());
					return;
				}
			}
		}
	}

	public boolean parametersReceived() {
		return motorPosition != -1;
	}

	public int getMotorPosition() {
		return motorPosition;
	}

	public boolean getMotorState()
	{
		return motorState == 1;
	}

	public Double getScopeTemp() {
		return scopeTemp;
	}

	public void setScopeTemp(Double scopeTemp) {
		this.scopeTemp = scopeTemp;
	}

	public Double getExtTemp() {
		return extTemp;
	}

	public void setExtTemp(Double extTemp) {
		this.extTemp = extTemp;
	}

	public Double getExtHum() {
		return extHum;
	}

	public void setExtHum(Double extHum) {
		this.extHum = extHum;
	}

	public Double getBattery() {
		return battery;
	}

	public void setBattery(Double battery) {
		this.battery = battery;
	}

	public Double getHeater() {
		return heater;
	}

	public void setHeater(Double heater) {
		this.heater = heater;
	}

	public int getMaxMotorPosition() {
		return 200000;
	}
}
