package fr.pludov.ardfocuser.driver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;

import fr.pludov.ardfocus.utils.WeakListenerCollection;

public class ThreadedServerSocket {
	
	public static interface ISocketServerListener {
		void newConnection(Socket socket);

		void serverDead();
	}

	public final WeakListenerCollection<ISocketServerListener> listeners = new WeakListenerCollection<>(ISocketServerListener.class);

	final ServerSocket serverSocket;
	boolean closed;
	final Thread consummer;
	
	public ThreadedServerSocket(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		closed = false;
		consummer = new Thread("TCP client listener") {
			@Override
			public void run() {
				consume();
			};
		};
		consummer.start();
	}
	
	public void close()
	{
		closed = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void consume()
	{
		while(true) {
			final Socket client;
			try {
				client = serverSocket.accept();
			} catch (final IOException e1) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						if (!closed) {
							e1.printStackTrace();
							close();
							listeners.getTarget().serverDead();
						}
					}
				});
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					if (!closed) {
						listeners.getTarget().newConnection(client);
					} else {
						try {
							client.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		} 
	}

}
