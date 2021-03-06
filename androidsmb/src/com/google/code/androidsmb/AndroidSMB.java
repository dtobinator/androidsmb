/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.androidsmb;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class AndroidSMB extends Activity implements AndroidSMBConstants, MessageListener {
	private boolean mIsRunning = false;
    private AndroidSMBService mService;
    private Handler mHandler;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    // Message types sent from the AndroidSMBService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_TOAST = 5;

    public static final int MESSAGE_WRITE = 3;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private static final int REQUEST_ENABLE_BT = 2;
    public static final String TOAST = "toast";

    private Button mButtonOn;
    // Name of the device
    private String mDeviceName = null;
    // Array adapter for the log thread
    private ArrayAdapter<String> mLogArrayAdapter;

    private ListView mLogView;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Layout Views
    private TextView mTitle;

//  // Local Bluetooth adapter
//  private BluetoothAdapter mBluetoothAdapter = null;

    private void log(String message){
    	if(DEBUG) Log.d(TAG, message);
        mLogArrayAdapter.add(message);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = ((AndroidSMBService.LocalBinder)service).getService();
            mService.getLogHandler().attach(AndroidSMB.this);
            // Tell the user about this for our demo.
            Toast.makeText(AndroidSMB.this, R.string.smb_service_connected,
                    Toast.LENGTH_SHORT).show();
            mIsRunning = mService.getStatus() == RUNNING;

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
        	mService.getLogHandler().dettach(AndroidSMB.this);
        	mService = null;
            Toast.makeText(AndroidSMB.this, R.string.smb_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHandler = new Handler();
        //
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        this.setupLog();
        this.log("+++ ON CREATE +++");
        
        
        
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
//

//
//        // Get local Bluetooth adapter
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        // If the adapter is null, then Bluetooth is not supported
//        if (mBluetoothAdapter == null) {
//            
//            finish();
//            return;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
//        if (mSMBService != null) mSMBService.stop();
        this.log("--- ON DESTROY ---");
        Toast.makeText(this, "Destroying", Toast.LENGTH_LONG).show();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        this.log("- ON PAUSE -");
    	// Detach our existing connection.
    	unbindService(mConnection);
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        this.log("+ ON RESUME +");
        bindService(new Intent(AndroidSMB.this, 
    			AndroidSMBService.class), mConnection, Context.BIND_AUTO_CREATE);
    	

//        // Performing this check in onResume() covers the case in which BT was
//        // not enabled during onStart(), so we were paused to enable it...
//        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
//        if (mChatService != null) {
//            // Only if the state is STATE_NONE, do we know that we haven't started already
//            if (mChatService.getState() == AndroidSMBService.STATE_NONE) {
//              // Start the Bluetooth chat services
//              mChatService.start();
//            }
//        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        this.log("++ ON START ++");

        Toast.makeText(this, "Creating", Toast.LENGTH_LONG).show();
        
//        // Set up the window layout
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//        setContentView(R.layout.main);
        
        // Initialize the send button with a listener that for click events
        mButtonOn = (Button) findViewById(R.id.button_on);
        mButtonOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                AndroidSMB.this.log("+++ Button Pressed +++");
                
                if(mIsRunning){
                	AndroidSMB.this.log("Stopping Service");
                	mService.shutDown();
                	stopService(new Intent(AndroidSMB.this, AndroidSMBService.class));
                	mIsRunning=false;

                } else {
                	AndroidSMB.this.log("Starting Service");
                	startService(new Intent(AndroidSMB.this, AndroidSMBService.class));
                	mIsRunning=true;
                }
                
//                if (mIsRunning) {
//                	AndroidSMB.this.log("Disconnecting");
//                	mIsRunning = false;
//                	stopService(new Intent(AndroidSMB.this, AndroidSMBService.class));
//
//                } else {
//                	AndroidSMB.this.log("Connecting");
//                	startService(new Intent(AndroidSMB.this, AndroidSMBService.class));
//                	mIsRunning = true;
//                }

                
            }
        });
//        // If BT is not on, request that it be enabled.
//        // setupChat() will then be called during onActivityResult
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        // Otherwise, setup the chat session
//        } else {
//            if (mChatService == null) setupChat();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.log("-- ON STOP --");
    }

    private void setupLog() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mLogArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mLogView = (ListView) findViewById(R.id.logView);
        mLogView.setAdapter(mLogArrayAdapter);
        this.log("Setup log view");
        
        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
        
//        // Initialize the compose field with a listener for the return key
//        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
//        mOutEditText.setOnEditorActionListener(mWriteListener);

    }

    private void setupService() {
        // Initialize the BluetoothChatService to perform bluetooth connections
//        mSMBService = new AndroidSMBService(this, mHandler);
    }

	public void error(final String msg) {
		mHandler.post(new Runnable(){

			public void run() {
				mLogArrayAdapter.add(ERROR+msg);
			}});
	}

	public void error(final String msg, final Throwable e) {
		mHandler.post(new Runnable(){

			public void run() {
				StringBuilder builder = new StringBuilder();
				mLogArrayAdapter.add(ERROR+msg+"\n");
				for(StackTraceElement element : e.getStackTrace()){
					mLogArrayAdapter.add(element.toString());
				}
			}});
	}

	public void message(final String msg) {
		mHandler.post(new Runnable(){

			public void run() {
				mLogArrayAdapter.add(msg);
			}});
	}

//    private void ensureDiscoverable() {
//        this.log("ensure discoverable");
//        if (mBluetoothAdapter.getScanMode() !=
//            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }
//    }

//    /**
//     * Sends a message.
//     * @param message  A string of text to send.
//     */
//    private void sendMessage(String message) {
//        // Check that we're actually connected before trying anything
//        if (mSMBService.getState() != AndroidSMBService.STATE_CONNECTED) {
//            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Check that there's actually something to send
//        if (message.length() > 0) {
//            // Get the message bytes and tell the BluetoothChatService to write
//            byte[] send = message.getBytes();
//            mSMBService.write(send);

//            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
//        }
//    }

//    // The action listener for the EditText widget, to listen for the return key
//    private TextView.OnEditorActionListener mWriteListener =
//        new TextView.OnEditorActionListener() {
//        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//            // If the action is a key-up event on the return key, send the message
//            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
//                String message = view.getText().toString();
//                sendMessage(message);
//            }
//            if(DEBUG) Log.i(TAG, "END onEditorAction");
//            return true;
//        }
//    };

//    // The Handler that gets information back from the BluetoothChatService
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                switch (msg.arg1) {
//                case AndroidSMBService.STATE_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mConnectedDeviceName);
//                    mLogArrayAdapter.clear();
//                    break;
//                case AndroidSMBService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
//                    break;
//                case AndroidSMBService.STATE_LISTEN:
//                case AndroidSMBService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    break;
//                }
//                break;
//            case MESSAGE_WRITE:
//                byte[] writeBuf = (byte[]) msg.obj;
//                // construct a string from the buffer
//                String writeMessage = new String(writeBuf);
//                mLogArrayAdapter.add("Me:  " + writeMessage);
//                break;
//            case MESSAGE_READ:
//                byte[] readBuf = (byte[]) msg.obj;
//                // construct a string from the valid bytes in the buffer
//                String readMessage = new String(readBuf, 0, msg.arg1);
//                mLogArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
//                break;
//            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                break;
//            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
//                break;
//            }
//        }
//    };

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(D) Log.d(TAG, "onActivityResult " + resultCode);
//        switch (requestCode) {
//        case REQUEST_CONNECT_DEVICE:
//            // When DeviceListActivity returns with a device to connect
//            if (resultCode == Activity.RESULT_OK) {
//                // Get the device MAC address
//                String address = data.getExtras()
//                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//                // Get the BLuetoothDevice object
//                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//                // Attempt to connect to the device
//                mSMBService.connect(device);
//            }
//            break;
//        case REQUEST_ENABLE_BT:
//            // When the request to enable Bluetooth returns
//            if (resultCode == Activity.RESULT_OK) {
//                // Bluetooth is now enabled, so set up a chat session
//                setupChat();
//            } else {
//                // User did not enable Bluetooth or an error occured
//                Log.d(TAG, "BT not enabled");
//                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.option_menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case R.id.scan:
//            // Launch the DeviceListActivity to see devices and do scan
//            Intent serverIntent = new Intent(this, DeviceListActivity.class);
//            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//            return true;
//        case R.id.discoverable:
//            // Ensure this device is discoverable by others
//            ensureDiscoverable();
//            return true;
//        }
//        return false;
//    }

}