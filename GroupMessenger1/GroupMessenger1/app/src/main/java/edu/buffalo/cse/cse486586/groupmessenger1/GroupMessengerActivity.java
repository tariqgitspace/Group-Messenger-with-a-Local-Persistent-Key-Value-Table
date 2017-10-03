package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.net.Uri;
import android.content.ContentValues;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    //tariq start

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String[] REMOTE_PORT_ARRAY = {"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    //tariq end
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);



        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));



        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }


        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */




        //tariq start

        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button button = (Button) findViewById(R.id.button4);


        button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v)
          {
              // taken from pa1 - SimpleMessenger
              String msg = editText.getText().toString() + "\n";
              editText.setText(""); // This is one way to reset the input box.
              TextView localTextView = (TextView) findViewById(R.id.textView1);
              localTextView.append(msg); // This is one way to display a string.
              //localTextView.append("\t" + msg); // This is one way to display a string.
              //TextView remoteTextView = (TextView) findViewById(R.id.textView1);
              localTextView.append("\n");

            /*
             * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
             * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
             * the difference, please take a look at
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
              new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
          }
      }
        );
        //tariq end

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }



    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        private Uri uri = null;

        private Uri buildUri(String scheme, String authority)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            int seq_number = 0;

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            //tariq Start
            Socket socket = null;


            while(true) {
                try {
                    socket = serverSocket.accept();
                    InputStream x = socket.getInputStream();
                    DataInputStream input_stream = new DataInputStream(x);
                    //Log.e(TAG, "Message Received at Server");
                    String Received_Data = input_stream.readUTF();

                    saveReceivedMessage(seq_number,Received_Data);
                    seq_number++;
                    publishProgress(Received_Data);



                    //Send Ack to Client for Received message
                    String msgToSend = "PA1-OK";
                    OutputStream out = socket.getOutputStream();
                    DataOutputStream send_data = new DataOutputStream(out);
                    send_data.writeUTF(msgToSend);
                    System.out.println("Message sent to the client is "+msgToSend);


                    //SystemClock.sleep(60);
                    send_data.flush();
                    socket.close();



                } catch (IOException e) {
                    Log.e(TAG, "IOException occured");
                }
            }

            //tariq end
            //return null;
        }

        protected void saveReceivedMessage(int seq_number, String Received_Data) {

            uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
            ContentValues key_value = new ContentValues();
            key_value.put("key", Integer.toString(seq_number));
            key_value.put("value", Received_Data);
            getContentResolver().insert(uri, key_value);
        }


        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            String strReceived = strings[0].trim();
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append(strings[0]);
            localTextView.append("\n");


            return;
            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */


            /*
            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
            */
        }
    }


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            try {

                String msgToSend = msgs[0];
                for(int i= 0 ; i< REMOTE_PORT_ARRAY.length;i++) {

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT_ARRAY[i]));

                    //tariq start
                    //https://developer.android.com/reference/java/io/ObjectOutputStream.html
                    OutputStream x = socket.getOutputStream();
                    DataOutputStream send_data = new DataOutputStream(x);
                    send_data.writeUTF(msgToSend);


                    //wait for ack from sever. Once received, send message to other server
                    while(true) {
                        InputStream in = socket.getInputStream();
                        DataInputStream input = new DataInputStream(in);
                        String message = input.readUTF();
                        if(message.equals("PA1-OK"))
                        {
                            System.out.println("Message received from the server : " + message);
                            break;
                        }
                    }

                    //SystemClock.sleep(500);
                    //send_data.flush();
                    socket.close();
                }


            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }



            return null;
        }
    }

}
