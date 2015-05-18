package com.wiredbadger.examples.boundservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This Activity will demonstrate 2-way communication between an
 * Activity and a Service using Messengers & Handlers
 */
public class MainActivity extends Activity {

    private static final String TAG = "Main-Activity";

    // The 'what' for messages from the service to this client
    static final int MSG_RESPONSE = 2;

    // The binder key for reponse messages
    static final String RESPONSE_KEY = "reponse-key";

    private EditText mSendMessageText;
    private TextView mResponseText;

    // The Messenger to send messages to the service
    private Messenger mServiceMessenger;

    // Are we bound to the service?
    private boolean mBound;


    /**
     * Class for communicating with the Hello Service
     */
    ServiceConnection mHelloServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            // We have bound to the service

            // Save reference to the Service's Messenger
            mServiceMessenger = new Messenger( service );
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceMessenger = null;
            mBound = false;
        }
    };


    /**
     * Handler the handles service response messages
     */
    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if( msg.what == MSG_RESPONSE) {
                Log.d(TAG, "Got a reponse!");
                Bundle b = msg.getData();
                String responseMsg = b.getString(RESPONSE_KEY);
                Log.d(TAG, "RX'ed message '" +responseMsg + "'");

                // Display response in UI
                mResponseText.setText(responseMsg);
            }
            else {
                super.handleMessage(msg);
            }
        }
    }

    // The messenger passed to the service to provide a Service to client path
    private final Messenger mRespMessenger = new Messenger(new ResponseHandler());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendMessageText = (EditText) findViewById(R.id.msg_text);
        mResponseText = (TextView) findViewById(R.id.response_text);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Starting");
        // Bind to the service
        bindService(new Intent(this, HelloService.class),
                    mHelloServiceConnection,
                    Context.BIND_AUTO_CREATE);
    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stopping");
        if( mBound ) {
            unbindService(mHelloServiceConnection);
            mServiceMessenger = null;
            mBound = false;
        }
    }

    /**
     * Handles send message click events
     * @param v View sending event
     */
    public void sendMessage(View v) {
        if( v.getId() == R.id.send_msg_button) {
            String msgText = mSendMessageText.getText().toString();
            if( mBound ) {
                Log.d(TAG, "Sending msg '" + msgText + "'");
                // Create the message to transmit
                Message message = Message.obtain(null, HelloService.MSG_HELLO, 0, 0);
                // Must put String into Bundle - only android.os.Parcelable objects can be transmitted
                Bundle bundle = new Bundle();
                bundle.putString(HelloService.MESSAGE_KEY, msgText);
                message.setData( bundle );
                // Add Activity's Messenger to message back to service to
                // provide service -> client comm link
                message.replyTo = mRespMessenger;
                // Transmit the message to the Service
                try {
                    mServiceMessenger.send( message );
                    Toast.makeText(this, "Sending Message", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    Log.e(TAG, "Exception :" + e.getMessage());
                    Toast.makeText(this, "Failed Sending Message!!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "Not bound to service!!", Toast.LENGTH_SHORT).show();
            }
        }

    }

}
