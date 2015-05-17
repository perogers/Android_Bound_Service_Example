package com.wiredbadger.examples.boundservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * Created by paulrogers on 5/17/15.
 *
 * This Service receives a message from a client and responds after a delay
 */
public class HelloService extends Service {

    private static final String TAG = "Hello-Service";

    static final int MSG_HELLO = 1;
    static final int MSG_REPLY_TO = 2;
    static final String MESSAGE_KEY = "message-key";

    // Delay before responding
    private static final long RESPONSE_DELAY = 5000L;

    private Messenger mClientMessenger;

    class RequestHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // This is a message from the client with text
            if( msg.what == MSG_HELLO) {
                Log.d(TAG, "Got a message!");
                Bundle b = (Bundle) msg.obj;
                String clientMsg = b.getString(MESSAGE_KEY);
                Log.d(TAG, "RX'ed message '" +clientMsg + "'");
                if( mClientMessenger != null) {
                    // Allow multiple messages to block until current message is processed
                    synchronized (this) {
                        try {
                            Thread.sleep( RESPONSE_DELAY );
                        } catch (InterruptedException e) {
                            // no op
                        }
                        Message respMsg = Message.obtain();
                        b = new Bundle();
                        b.putString(MainActivity.RESPONSE_KEY, "Hey there!");
                        respMsg.obj = b;
                        respMsg.what = MainActivity.MSG_RESPONSE;
                        try {
                            Log.d(TAG, "Sending reponse");
                            mClientMessenger.send(respMsg);
                        } catch (Exception e) {
                            Log.e(TAG, "Error sending response to client: " + e.getMessage());
                        }
                    }
                }
                else {
                    Log.e(TAG, "No client response messenger available!");
                }

            }
            // This is a message from the client with a Messenger
            else if ( msg.what == MSG_REPLY_TO) {
                Log.d(TAG, "Received client messenger");
                mClientMessenger = msg.replyTo;
            }
            // All others
            else {
                super.handleMessage(msg);
            }
        }
    }


    final Messenger mMessenger = new Messenger( new RequestHandler());


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding");
        return mMessenger.getBinder();
    }



}
