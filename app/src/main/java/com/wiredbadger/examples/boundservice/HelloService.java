package com.wiredbadger.examples.boundservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by paulrogers on 5/17/15.
 *
 * This Service receives a message from a client and responds after a delay
 */
public class HelloService extends Service {

    private static final String TAG = "Hello-Service";

    static final int MSG_HELLO = 1;
    static final String MESSAGE_KEY = "message-key";

    // Delay before responding
    private static final long RESPONSE_DELAY = 3000L;

    // The client Messenger
    private WeakReference<Messenger> mMessengerWeakReference;

    /**
     * The Handler to receive client messages
     */
    class RequestHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // This is a message from the client with text
            synchronized (this) {
                if (msg.what == MSG_HELLO) {
                    Log.d(TAG, "Got a message!");
                    obtainClientMessenger(msg);
                    if( mMessengerWeakReference == null || mMessengerWeakReference.get() == null)
                        return;

                    String clientMsg = obtainMessageText(msg);
                    Log.d(TAG, "RX'ed message '" + clientMsg + "'");
                    // Allow multiple messages to block until current message is processed
                    try {
                        Thread.sleep(RESPONSE_DELAY);
                    } catch (InterruptedException e) {
                        // no op
                    }
                    Message respMsg = buildReponseMessage(clientMsg);

                    try {
                        Log.d(TAG, "Sending reponse");
                        mMessengerWeakReference.get().send(respMsg);
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending response to client: " + e.getMessage());
                    }
                }
                // All others
                else{
                    super.handleMessage(msg);
                }
            }

        }

    }



    /**
     * Extracts Messenger from client message's replyto attribute
     * @param message the client message
     */
    private void obtainClientMessenger(Message message) {
        if( message.replyTo != null) {
            Log.d(TAG, "Received client messenger");
            mMessengerWeakReference = new WeakReference<Messenger>(message.replyTo);
        }
        else {
            Log.e(TAG, "No client messenger received!");
        }
    }

    /**
     * Obtain the client's text message
     * @param message the client message
     * @return text string
     */
    private String obtainMessageText(Message message) {
        Bundle b = (Bundle) message.obj;
       return b.getString(MESSAGE_KEY);
    }

    /**
     * Creates a response message to return to client
     * @return Message object
     */
    private Message buildReponseMessage(String clientMsg){
        Message respMsg = Message.obtain();
        Bundle b = new Bundle();
        b.putString(MainActivity.RESPONSE_KEY, "You sent: '" + clientMsg + "'");
        respMsg.obj = b;
        respMsg.what = MainActivity.MSG_RESPONSE;
        return respMsg;
    }


    // The Messenger for client to this Service communication
    final Messenger mMessenger = new Messenger( new RequestHandler());


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding");
        return mMessenger.getBinder();
    }



}
