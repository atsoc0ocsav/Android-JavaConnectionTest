package pt.iscte.biomachineslab.androidside;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends Activity {
    private static final String IP_ADDRESS_REGEX = "^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$";
    private static final String HOSTNAME_REGEX = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    private ClientThread connection = null;

    private static final int SERVERPORT = 5000;
    private String ipAddr = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void onClickSend(View view) {
        EditText toSendTextArea = (EditText) findViewById(R.id.toSendTextArea);
        String toSendText = toSendTextArea.getText().toString();

        if(toSendText!=null ) {
            connection.sendMessage(toSendText);
        }
    }

    public void onClickConnect(View view) {
        if (((Button) view).getText().toString().equals(getString(R.string.connectButton))) {
            EditText ipAddrTextArea = (EditText) findViewById(R.id.ipAddrTextArea);
            String ipAddrText = ipAddrTextArea.getText().toString();

            if(ipAddrText!=null && (ipAddrText.matches(HOSTNAME_REGEX) || ipAddrText.matches(IP_ADDRESS_REGEX))){
                ipAddr=ipAddrText;
                connection = new ClientThread();
                connection.start();

                ((Button) view).setText(R.string.disconnectButton);
            }
        } else {
            connection.shutdownClient();
            connection=null;

            ((Button) view).setText(R.string.connectButton);
        }
    }

    class ClientThread extends Thread {
        private Socket socket;
        //private PrintWriter in;
        private PrintWriter out;

        private boolean shutdownClient=false;

        @Override
        public void run() {
            InetAddress serverAddr = null;
            try {
                serverAddr = InetAddress.getByName(ipAddr);
                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e) {
                System.err.println("Unable to reach " + ipAddr);
            } catch (IOException e) {
                System.err.println("Unable to open a connection to " + ipAddr + ":" + SERVERPORT);
            }

            try {

                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                //in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error opening stream channels!");
            }

            while(!shutdownClient){
                try {
                    wait();
                }catch(InterruptedException e){
                    System.err.println("Interrupted while waiting!");
                }
            }
        }

        public void shutdownClient(){
            shutdownClient=true;
            interrupt();
        }

        public void sendMessage(String message){
            out.print(message);
        }
    }
}
