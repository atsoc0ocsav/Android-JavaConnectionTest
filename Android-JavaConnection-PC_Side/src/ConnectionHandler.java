
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectionHandler extends Thread {

	private final static boolean DEBUG = false;

	protected Socket socket;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected String clientName = null;
	protected ConnectionListener connectionListener;

	public ConnectionHandler(Socket socket,
			ConnectionListener connectionListener) {
		this.socket = socket;
		this.connectionListener = connectionListener;
	}

	@Override
	public void run() {
		try {
			initConnection();

			while (true) {

			}

		} catch (IOException e) {
			System.out.println("Client "
					+ socket.getInetAddress().getHostAddress() + " ("
					+ clientName + ") disconnected");
		} catch (ClassNotFoundException e) {
			System.out.println("I didn't reveived a correct name from "
					+ socket.getInetAddress().getHostAddress());
			e.printStackTrace();
		} finally {
			// always shutdown the handler when something goes wrong
			shutdownHandler();
		}
	}

	protected void shutdownHandler() {
		closeConnection();
	}

	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		out.reset();

		out.writeObject(InetAddress.getLocalHost().getHostName());
		out.flush();

		clientName = (String) in.readObject();

		System.out.println("[CONNECTION HANDLER] Client "
				+ socket.getInetAddress().getHostAddress() + " (" + clientName
				+ ") connected");

	}

	public synchronized void closeConnection() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				connectionListener.removeConnection(this);
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out
					.println("[CONNECTION HANDLER] Unable to close connection to "
							+ clientName + "... there is an open connection?");
		}
	}

	public synchronized void closeConnectionWthoutDiscardConnListener() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out
					.println("[CONNECTION HANDLER] Unable to close connection to "
							+ clientName + "... there is an open connection?");
		}
	}

	public synchronized void sendData(Object data) {
		try {
			out.reset();// clear the cache so that we don't send old data
			out.writeObject(data);
			out.flush();
			if (DEBUG)
				System.out.println("[CONNECTION HANDLER] Sent Data ("
						+ data.getClass().getSimpleName() + ")");
		} catch (IOException e) {
			System.out
					.println("[CONNECTION HANDLER] Unable to send data... there is an open connection?");
		}
	}

	public Socket getSocket() {
		return socket;
	}
}
