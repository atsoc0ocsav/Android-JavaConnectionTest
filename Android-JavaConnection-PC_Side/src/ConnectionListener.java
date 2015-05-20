

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionListener extends Thread {

	private static final int DEFAULT_PORT = 10110;
	protected ArrayList<ConnectionHandler> connections = new ArrayList<>();
	protected int port;
	protected ServerSocket serverSocket = null;
	private AtomicBoolean enable = new AtomicBoolean();

	public ConnectionListener() throws IOException {
		this(DEFAULT_PORT);
	}

	public ConnectionListener(int port) throws IOException {
		this.port = port;

		serverSocket = new ServerSocket(port);
	}

	@Override
	public void run() {
		try {
			System.out
					.println("[CONNECTION LISTENER] Connection Handler Initialized on "
							+ InetAddress.getLocalHost().getHostAddress() + ":" + port);
			System.out
					.println("[CONNECTION LISTENER] Waiting for connection requests!");
			while (enable.get()) {
				Socket socket = serverSocket.accept();
				createHandler(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out
						.println("[CONNECTION LISTENER] Unable to close server socket.... there was an open socket?");
			}
		}
	}

	protected void addConnection(ConnectionHandler conn) {
		connections.add(conn);
	}

	protected void createHandler(Socket s) {
		ConnectionHandler conn = new ConnectionHandler(s, this);
		addConnection(conn);
		conn.start();
	}

	public void closeConnections() {
		if (!connections.isEmpty()) {
			System.out.println("[CONNECTION HANDLER] Closing Connections!");
			for (ConnectionHandler conn : connections) {
				if (!conn.getSocket().isClosed())
					conn.closeConnectionWthoutDiscardConnListener();
			}
		}
	}

	@Override
	public synchronized void start() {
		enable.set(true);
		super.start();
	};
	
	public void shutdown() {
		enable.set(false);
		closeConnections();
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out
					.println("[CONNECTION HANDLER] Unable to close server socket.... there was an open socket?");
		}
	}

	public synchronized void removeConnection(ConnectionHandler conn) {
		connections.remove(conn);
	}

	public ArrayList<ConnectionHandler> getConnections() {
		return connections;
	}
}
