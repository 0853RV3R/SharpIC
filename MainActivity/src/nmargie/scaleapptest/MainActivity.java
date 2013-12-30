package nmargie.scaleapptest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;




import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener{

	private static final boolean D = true;
	private static final String TAG = "MainActivity";
	SharedPreferences.Editor edit;
	SharedPreferences mPrefs;
	public static final String PREFS_NAME = "ScanPrefs";
	public static ArrayList<Entry> dataList = new ArrayList<Entry>();
	ListView l;
	Button btnRead, btnHold, btnTare;
	TextView txtRcvData, tvScaleCommand, tvConnection, tvConnectionScanner;
	LinearLayout mDataView, mUploadStatus;
	Spinner spinVenue, spinRoom;
	
	public static int venue, room;
	boolean OZflag = false;
	/*
	 * PRE onCreate Declarations form SDK
	 * 
	 */
	//private TextView mTitle;
	
	// Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Additional message types sent from BluethoothChatService Handler
    public static final int MESSAGE_DISPLAY = 6;
    public static final int MESSAGE_SEND = 7;
    
    public static final int MESSAGE_SETTING = 255;
    public static final int MESSAGE_EXIT = 0;
    
    private byte[] displayBuf = new byte[256];
    private String displayMessage;
    
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_SCANNER = 3;
    
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	
  //Bluetooth commands for Plus BT2 scales
    private static byte[] cmdReadWeightOnce				= new byte[] {0x07, 0x00, 0x11};
    private static byte[] cmdReadWeightCont				= new byte[] {0x07, 0x00, 0x01};
    private static byte[] cmdStopSendCmd				= new byte[] {0x07, 0x00, 0x0F};
    private static byte[] cmdTareWeight					= new byte[] {0x07, 0x00, 0x72};
    private static byte[] cmdSetUnitG					= new byte[] {0x07, 0x00, 0x52};
    private static byte[] cmdSetUnitLBOZ				= new byte[] {0x07, 0x00, 0x5B};
    private static byte[] cmdSetOffTimer				= new byte[] {0x07, 0x00, 0x7E};
    private static byte[] cmdReadBatLevel				= new byte[] {0x07, 0x00, 0x7D};
    private static byte[] cmdGetScaleConfig				= new byte[] {0x07, 0x00, 0x78};
    private static byte[] cmdReadSN						= new byte[] {0x07, 0x00, 0x7C};
    private static byte[] cmdTurnOff					= new byte[] {0x07, 0x00, 0x7F};
    
    //command list for reading results from scale output
    private static final int CMD_READ_ONCE = 1;
    private static final int CMD_READ_CONT = 2;
    private static final int CMD_STOP_SEND = 3;
    private static final int CMD_TARE_WT = 4;
    private static final int CMD_SET_GR = 5;
    private static final int CMD_SET_LBOZ = 6;
    private static final int CMD_SET_TIMER = 7;
    private static final int CMD_READ_BATLEVEL = 8;
    private static final int CMD_GET_CONFIG = 9;
    private static final int CMD_READ_SN = 10;
    private static final int CMD_TURN_OFF = 11;
    
    private static final byte BYTE_ACK = (byte) 0xFF; //command acknowledged
    private static final byte BYTE_NAK = 0x00; //command not acknowledged
    private static final byte BYTE_ID = 0x03; //this is always the first byte of data coming from scale
    
    //overload weight - if greater than these values, scale is overloaded
    private static final double WT_OL_GR = 5010.0; //5010 grams if scale is in gram mode
    private static final double WT_OL_OZ = 176.7; //11lb 0.7 if scale is in gram mode
    
    // State variables
    private boolean paused = false;
    private boolean connected = false;
    private int intCmdIndex = 0;
    private int intOffTimer = 0;
    
	// Name of the connected device
    private String mConnectedDeviceName = null;
    private String mConnectedScannerName = null;
    
    // String buffer for outgoing messages -- SCALE
    private StringBuffer mOutStringBuffer;
    private StringBuffer mOutStringBufferScanner;
    // Local Bluetooth adapter -- try to get it to handle both devices (scanner and scale)
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the Bluetooth SCALE services
    private BluetoothScaleService mBluetoothService = null;
	// Member object for Bluetooth SCANNER services
    //private BluetoothChatService mBTScanService = null;
    public BluetoothDevice deviceScanner = null;  
    private AutoUploadTask mAutoUploadTask = null;
    
	/*
	 * END == PRE onCreate Declarations form SDK
	 * 
	 */
	private NickAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(D) Log.e(TAG, "+++onCreate+++");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main); // set background layout
		
		/*
		 * TRYING TO GET CUSTOM TITLE WORKING...
		 * 
		 * 
		 * 
		 */
		/*
		// Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
	
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text1);
        txtRcvData = (TextView) findViewById(R.id.txtRcvData);
		
		*/
		
		
		
		/*
		 * END OF TRYING TO GET CUSTOM TITLE WORKING
		 * 
		 * 
		 */
		
		
		venue = -1;
		room = -1;
		if (dataList.isEmpty()) {
			populateEntries(); // for testing purposes -- fills up AraryList<Entry> if it is empty (i.e. first time)
		}
		else{
			getPrefs(); // retrieve dataList
		}
		KTSyncData.bIsRunning = true;
		initViews();
		
		
		
		adapter = new NickAdapter(this, dataList); //bind adapter to dataList once and for good (final)
		l.setAdapter(adapter); // bind adapter to listView
		
		// TODO: need to set the venue and room based on spinners with STEVE
		// venue string array resource to give data to venue spinner
		ArrayAdapter<CharSequence> venueAdapter = ArrayAdapter.createFromResource(this, R.array.venue_array, android.R.layout.simple_spinner_item);
		venueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//spinner for venues set to adapter to bind data
		spinVenue.setAdapter(venueAdapter);
		
		
		/*
		 * 
		 * SDK STUFF
		 */
		// Get local Bluetooth adapter -- USED FOR BOTH SCALE AND SCANNER
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported -- end the app
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        /*
		 * END === SDK STUFF
		 * 
		 */
        KTSyncData.mKScan = new KScan(this, mHandlerScanner); // initialize KScan class
        KTSyncData.BufferRead = 0;
        KTSyncData.BufferWrite = 0;
        KTSyncData.bIsRunning = true;
        for (int i = 0; i < 10; i++) {
        	KTSyncData.SerialNumber[i] = '0';
        	KTSyncData.FWVersion[i] = '0';
        }
        
        KTSyncData.bIsOver_233 = false;
        
        StringBuffer buf = new StringBuffer();
        buf.append(Build.VERSION.RELEASE);
        
         
        String version = buf.toString();
        String target = "2.3.3";
        if ( version.compareTo(target) > 0 ) {
        	KTSyncData.bIsOver_233 = true;
        }      
        
	} // end onCreate
	
	// for testing purposes
	private void populateEntries() {
		
		
		for (int i = 0 ; i<10; i++ ){
			dataList.add(new Entry(i, 00, 05,"15."+ i,"0009900"+i, new Date(), System.currentTimeMillis() / 1000L));
			
			if (i<5) dataList.get(i).uploaded=true;
		}
		
	}
	

	@Override
	protected void onPause() {
		// Save persistent data to sharedPrefs
		if(D) Log.e(TAG, "+++onPause+++");
		super.onPause();
		
		setPrefs();
		
		
	}
	private void setPrefs() {
		if(D) Log.e(TAG, "+++setPrefs+++");
		mPrefs = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		edit = mPrefs.edit();
		edit.clear();
		edit.commit();
		try{
			edit.putString("Entries", ObjectSerializer.serialize(dataList));
		}
		catch (IOException e){
			e.printStackTrace();
		}
		edit.commit();
	}

	
	private void getPrefs() {
		// TODO Auto-generated method stub
		if(D) Log.e(TAG, "+++getPrefs+++");
		mPrefs = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		try {
            dataList = (ArrayList<Entry>) ObjectSerializer.deserialize(mPrefs.getString("Entries", ObjectSerializer.serialize(new ArrayList<Entry>())));
        } catch (IOException e) {
            e.printStackTrace();
        } 
		
	}

	private void initViews() {
		// initialize the views in the layout
		if(D) Log.e(TAG, "+++initViews+++");
		System.out.println("+++initViews+++");
		l =  (ListView) findViewById(R.id.listView);
		btnRead = (Button) findViewById(R.id.btnRead);// initially set as invisible in XML layout file -- it must toggle visibility with the Hold button
		btnHold = (Button) findViewById(R.id.btnHold);
		btnTare = (Button) findViewById(R.id.btnTare);
		tvScaleCommand = (TextView) findViewById(R.id.tvScaleCommand);
		txtRcvData = (TextView) findViewById(R.id.txtRcvData);
		tvConnection = (TextView) findViewById(R.id.tvConnection);
		tvConnectionScanner = (TextView) findViewById(R.id.tvConnectionScanner);
		mDataView = (LinearLayout) findViewById(R.id.mDataView);
		mUploadStatus = (LinearLayout) findViewById(R.id.mUploadStatus);
		// SPINNERS
		spinVenue = (Spinner) findViewById(R.id.venue_spinner);
		spinRoom =  (Spinner) findViewById(R.id.room_spinner);
	}
	
	// SPINNER METHODS
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	
	@Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        System.out.println("+++ ON START +++");
        // If BT is not on, request that it be enabled.
        // setupUserInterface() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the Bluetooth session
        } else {
            if (mBluetoothService == null || KTSyncData.mChatService == null) setupUserInterface();  // start instances of BluetoothService with Handler
        }
    }
	
	private void setupUserInterface() {
		if(D) Log.e(TAG, "+++setupUserInterface+++");
		System.out.println("+++setupUserInterface+++");
		// Initialize the BluetoothService to perform Bluetooth connections on SCALE
        mBluetoothService = new BluetoothScaleService(mHandler);
     // Initialize the BluetoothService to perform Bluetooth connections on SCANNER
        //mBTScanService = new BluetoothChatService(getApplicationContext(), mHandlerScanner);
        KTSyncData.mChatService = new BluetoothChatService(this, mHandlerScanner);
        
       
        // Initialize the buffers for outgoing messages (used in sendMessage and sendMessageScanner)
        mOutStringBuffer = new StringBuffer("");
        mOutStringBufferScanner = new StringBuffer("");
	}

	// The Handler that gets information back from the BluetoothScaleService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                System.out.println("MESSAGE_STATE_CHANGE: " + msg.arg1);
                
                switch (msg.arg1) {
                case BluetoothScaleService.STATE_CONNECTED:
                	if(D) Log.e(TAG, "+++Handler STATE CONNECTED+++");
                	System.out.println("+++Handler STATE CONNECTED+++");
                	connected = true;
                	tvConnection.setText("Connected.");
                	break;
                	/*
                    mTitle.setText(mConnectedDeviceName);
                    break;
                    */
                case BluetoothScaleService.STATE_CONNECTING:
                    tvConnection.setText("Connecting...");
                    break;
                    
                case BluetoothScaleService.STATE_NONE:
                	if(D) Log.e(TAG, "+++Handler STATE DISCONNECTED+++");
                	System.out.println("+++Handler STATE DISCONNECTED+++");
                	connected = false;
                    //mTitle.setText("Scale not Connected");
                	tvConnection.setText("Disconnected.");
                    break;
                    
                }
                break;
                //onBluetoothStateChanged(); // changes bottom UI toolbar to reflect scale connection status
                
                
                
            case MESSAGE_WRITE:
            	if (D) Log.d(TAG, "+++MESSAGE_WRITE+++");
            	//display the name of command that was just sent to the scale
            	//String writeMessage = getResources().getStringArray(R.array.btscale_commands)[intCmdIndex-1]; //spnCommands.getSelectedItem().toString();
                //txtSendData.setText("Last Command Sent:\n\r>>> " + writeMessage + "\n\r");
                //txtSendData.setText("Last Command Sent:\n\r>>> " + writeMessage + "\n\r" + mOutEditText.getText().toString() + "\n\r"); //enable if want to show the bytes sent to scale
                //if (D) Log.d(TAG, "written = '" + writeMessage + "'");
                break;
            case MESSAGE_READ:
            	if(D) Log.d(TAG, "+++Handler MESSAGE READ+++");
            	System.out.println("+++Handler MESSAGE READ+++");
            	//if (paused) break;
                byte[] readBuf = (byte[]) msg.obj;
                txtRcvData.setText("");
                String readMessage = "";
                String strBuffer = "";
                for(int i = 0; i < readBuf.length; i++)
                {
                	strBuffer = strBuffer + "0x" + String.format("%02X", readBuf[i]) + "-"; //bytes received from scale
                }
                readMessage = ConvertByteToStringData(readBuf); //show scale data only
                //readMessage = readMessage + "\n\rByte Data Received: " + strBuffer; //enable if want to show the complete data buffer from scale
                if (D) Log.d(TAG, "setting textview for scale data: "+readMessage);
                System.out.println(readMessage);
                txtRcvData.setText(readMessage);
                
                break;
            case MESSAGE_DEVICE_NAME:
            	if(D) Log.d(TAG, "+++Handler MESSAGE DEVICE NAME+++");
            	System.out.println("+++Handler MESSAGE DEVICE NAME+++");
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
            	if(D) Log.d(TAG, "+++Handler MESSAGE TOAST+++");
            	System.out.println("+++Handler MESSAGE TOAST+++");
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
    public void clickedRead(View v){
		if(D) Log.d(TAG, "+++clickedRead+++");
		System.out.println("+++clickedRead+++");
		if(mBluetoothService.getState() == BluetoothScaleService.STATE_CONNECTED){
			sendBTCommand(CMD_READ_CONT);
			//View btnRead = findViewById(R.id.btnRead);
			btnRead.setVisibility(View.INVISIBLE);
			//View btnHold = findViewById(R.id.btnRead);
			btnHold.setVisibility(View.VISIBLE);
			Toast.makeText(getApplicationContext(), "Read button clicked.", Toast.LENGTH_SHORT).show();
		}
		else Toast.makeText(getApplicationContext(), "Cannot Read: Scale not connected.", Toast.LENGTH_SHORT).show();
		
		
	}
	
	/*
     * function: ConvertByteToStringData
     * parameter: array of bytes to parse
     * return value: string data parsed from this array of bytes
     * description: function that reads the output from Elane BT scales, parse and interprets them according to command sent to scale
     * 				also displays the value for other functions like read battery level and scale serial number
     */
    public String ConvertByteToStringData(byte byteData[])
    {
    	if(D) Log.d(TAG, "+++ConvertByteToStringData+++");
    	System.out.println("+++ConvertByteToStringData+++");
    	String strTemp = "";
    	long lngValue = 0;
    	double dblValue = 0;
    	String sTemp = "";
    	if(byteData[0]==BYTE_ID) //check if first byte is valid
    	{
    		//check if data sent by scale is weight - then regardless of command sent, display this weight data
    		if(byteData[2]==0x52 || byteData[2]==0x5B)//g or ounce
    		{
    			lngValue = ((byteData[4]&0xFF) * (256*256*256)) + ((byteData[5]&0xFF) *(256*256)) + ((byteData[6]&0xFF) * 256) + (byteData[7]&0xFF);
    			if(byteData[4]==0xFF)//negative weight
    			{
    				lngValue = ((byteData[5]&0xFF) * (256*256)) + ((byteData[6]&0xFF) * 256) + (byteData[7]&0xFF);
    				lngValue = (~lngValue & 0x00ffff) + 1;
    				lngValue = lngValue * -1;
     			}
    			switch(byteData[3]) //scaling
    			{
    				case (byte) 0xFE: //0.01 scaling
    					dblValue = (double)lngValue * 0.01;
    					sTemp = String.format(("%.2f"), dblValue);
    					break;
    				case (byte) 0xFF: //0.1 scaling
    					dblValue = (double)lngValue * 0.1;
    					sTemp = String.format(("%.1f"), dblValue);
    					break;
	    			case (byte) 0x01: //10 scaling
	    				dblValue = (double)lngValue * 10;
	    				sTemp = String.format(("%.0f"), dblValue);
	    				break;
	    			case (byte) 0x00: // no scaling
	    				dblValue = (double)lngValue;
	    				sTemp = String.format(("%.0f"), dblValue);
	    				break;    					
	    			default:
	    				dblValue = (double)lngValue;
	    				break;
    			}
    			//check for overload weight, depending on the unit that is being used by the scale
    			//overload if weight is greater than 5010 grams when in g unit or 176.7 oz when in lb/oz
    			if(byteData[2]==0x52) //weight unit
    			{
    				if(dblValue<=WT_OL_GR) //gram
    					strTemp = sTemp + " g";
    				else
    					strTemp = "OVER LOAD";
    			}
    			else if(byteData[2]==0x5B) //oz
    			{
    				if(dblValue<=WT_OL_OZ)
 	    				strTemp = sTemp + " oz";
     				else
    					strTemp = "OVER LOAD";
    			}
     		}
    		else //other commands, then check if command is received and acknowledged by the scale (scale returns ACK)
    		{
	    		switch(intCmdIndex)
	    		{
	    			case CMD_STOP_SEND: //scale does not return any data
	    			break;
	    			case CMD_TARE_WT: //scale returns ACK flag and tare weight
	    				if(byteData[2]==BYTE_ACK)
	    					Toast.makeText(this, "Tare Successful", Toast.LENGTH_SHORT).show();
	    				else if(byteData[2]==BYTE_NAK)
	    					Toast.makeText(this, "Tare Unsuccessful", Toast.LENGTH_SHORT).show();
	    				strTemp = ""; //nothing to return;
					break;
	    			case CMD_SET_GR: //scale returns ACK flag
	    				if(byteData[2]==BYTE_ACK)
	    					Toast.makeText(this, "Unit Set to Gram", Toast.LENGTH_SHORT).show();
	    				else if(byteData[2]==BYTE_NAK)
	    					Toast.makeText(this, "Failed to set unit to gram", Toast.LENGTH_SHORT).show();
	    				strTemp = ""; //nothing to return;
					break;
	    			case CMD_SET_LBOZ: //scale returns ACK flag
	    				if(byteData[2]==BYTE_ACK)
	    					Toast.makeText(this, "Unit Set to LB/OZ", Toast.LENGTH_SHORT).show();
	    				else if(byteData[2]==BYTE_NAK)
	    					Toast.makeText(this, "Failed to set unit to LB/OZ", Toast.LENGTH_SHORT).show();
	    				strTemp = ""; //nothing to return;
					break;
	    			case CMD_SET_TIMER: //scale returns ACK flag	
	    				/*
	    				strTemp = getResources().getString(R.string.auto_offtimer_success).toString();
	    				if(intOffTimer>1)
	    					strTemp = strTemp + " " + Integer.toString(intOffTimer) + " minutes.";
	    				else
	    					strTemp = strTemp + " " + Integer.toString(intOffTimer) + " minute.";
	    				Toast.makeText(this, strTemp, Toast.LENGTH_LONG).show();
	    				strTemp = Integer.toString(intOffTimer);
	    				*/
	    			break;
	    			
	    			case CMD_READ_BATLEVEL: ////scale returns ACK flag and battery level value
	    				/*
	    				if(byteData[2]==BYTE_ACK)
	    				{
	    					lngValue = ((byteData[4]&0xFF)*(256*256*256)) + ((byteData[5]&0xFF)*(256*256)) + ((byteData[6]&0xFF)*256 + (byteData[7]&0xFF));
	    					strTemp = getResources().getString(R.string.read_batlevel).toString();
	    					strTemp = strTemp + " " + Long.toString(lngValue) + "%";
	     					Toast.makeText(this, strTemp, Toast.LENGTH_LONG).show();
	     					strTemp = Long.toString(lngValue);
	    				}
	    				else if(byteData[2]==BYTE_NAK)
	    					Toast.makeText(this, R.string.send_command_failed, Toast.LENGTH_SHORT).show();	
	    					*/    				
					break;
	    			case CMD_GET_CONFIG: //scale returns division, ACK flag, scaling, and capacity
	    				/*
	    				if(byteData[2]==BYTE_ACK)
	    				{
	    					dblValue = (double)(byteData[1]&0xFF); //division
	    					lngValue = ((byteData[4]&0xFF)*(256*256*256)) + ((byteData[5]&0xFF)*(256*256)) + ((byteData[6]&0xFF)*256 + (byteData[7]&0xFF));
	    					strTemp = Long.toString(lngValue) + "g/";
	    					//check scaling
	    					if(byteData[3]==0xFE) //0.01
	    					{
	    						dblValue = dblValue * 0.01;
	    						sTemp = String.format(("%.2f"), dblValue);
	    						strTemp = strTemp + sTemp + "g";
	    					}
	    					else if(byteData[3]==0xFF)//0.01   
	    					{
	    						dblValue = dblValue * 0.1;
	    						sTemp = String.format(("%.1f"), dblValue);
	       						strTemp = strTemp + sTemp + "g";
	    					}
	    					else if(byteData[3]==0x00)//1
	    					{
	    						dblValue = dblValue * 1;
	    						sTemp = String.format(("%.0f"), dblValue);
	       						strTemp = strTemp + sTemp + "g";
	    					}
	    					else if(byteData[3]==0x01)//10
	    					{
	    						dblValue = dblValue * 10;
	    						sTemp = String.format(("%.0f"), dblValue);
	       						strTemp = strTemp + sTemp + "g";
	    					}
	    					Toast.makeText(this, getResources().getString(R.string.read_scaleconfig).toString() + " " + strTemp, Toast.LENGTH_LONG).show();
	    				}
	    				else if(byteData[2]==BYTE_NAK)
	    					Toast.makeText(this, R.string.send_command_failed, Toast.LENGTH_SHORT).show();
	    					*/
					break;
	    			case CMD_READ_SN: //scale returns ACK flag and serial number
	    				/*
	    				if(byteData[2]==BYTE_ACK)
	    				{
	    					lngValue = ((byteData[4]&0xFF)*(256*256*256)) + ((byteData[5]&0xFF)*(256*256)) + ((byteData[6]&0xFF)*256 + (byteData[7]&0xFF));
	    					strTemp = getResources().getString(R.string.read_serialnumber).toString();
	    					strTemp =  strTemp + " " + Long.toString(lngValue);
	    					Toast.makeText(this, strTemp, Toast.LENGTH_LONG).show();
	    					strTemp = Long.toString(lngValue);
	    				}
	    				else if(byteData[2]==BYTE_NAK)
	    					Toast.makeText(this, R.string.send_command_failed, Toast.LENGTH_SHORT).show();	
	    					*/    				
					break;				
	    			case CMD_TURN_OFF: //scale can not return any data
					break;
	    		}
    		}
    	}
    	return strTemp; // string value returned
   }
	
	public void clickedHold(View v){
		if(D) Log.d(TAG, "+++clickedHold+++");
		System.out.println("+++clickedHold+++");
		if(mBluetoothService.getState() == BluetoothScaleService.STATE_CONNECTED){
			sendBTCommand(CMD_STOP_SEND);
			Toast.makeText(getApplicationContext(), "Hold button clicked.", Toast.LENGTH_SHORT).show();
		//View btnRead = findViewById(R.id.btnRead);
		btnRead.setVisibility(View.VISIBLE);
		//View btnHold = findViewById(R.id.btnRead);
		btnHold.setVisibility(View.INVISIBLE);
		}
		else Toast.makeText(getApplicationContext(), "Cannot Hold: Scale not connected.", Toast.LENGTH_SHORT).show();
	}
	
	public void clickedTare(View v){
		if(D) Log.d(TAG, "+++clickedTare+++");
		System.out.println("+++clickedTare+++");
		if(mBluetoothService.getState() == BluetoothScaleService.STATE_CONNECTED){
			sendBTCommand(CMD_TARE_WT);
			Toast.makeText(getApplicationContext(), "Tare button clicked.", Toast.LENGTH_SHORT).show();
		}
		else Toast.makeText(getApplicationContext(), "Cannot Tare: Scale not connected.", Toast.LENGTH_SHORT).show();
		
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(D) Log.d(TAG, "+++onCreateOptionsMenu+++");
		System.out.println("+++onCreateOptionsMenu+++");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
        switch (item.getItemId()) {
        case R.id.menu_grams:
        	if(D) Log.d(TAG, "+++menu_grams+++");
        	System.out.println("+++menu_grams+++");
        	// send command if BT connected 
        	if(mBluetoothService.getState() == BluetoothScaleService.STATE_CONNECTED){
        		sendBTCommand(CMD_SET_GR);
        		if (item.isChecked()) item.setChecked(false);
        		else item.setChecked(true);
        		OZflag = false;
        	}
        	else Toast.makeText(getApplicationContext(), "Can't set units (g) -- scale not connected", Toast.LENGTH_SHORT).show();
        	break;
        case R.id.menu_lboz:
        	if(D) Log.d(TAG, "+++menu_lboz+++");
        	System.out.println("+++menu_lboz+++");
        	if(mBluetoothService.getState() == BluetoothScaleService.STATE_CONNECTED){
        		if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
        		sendBTCommand(CMD_SET_LBOZ);
        		OZflag = true;
        	}
        	else Toast.makeText(getApplicationContext(), "Can't set units (lb/oz) -- scale not connected", Toast.LENGTH_SHORT).show();
        	break;
        case R.id.menu_scale_connect:
        	if(D) Log.d(TAG, "+++menu_connect+++");
        	System.out.println("+++menu_scale_connect+++");
        	// TODO: if scale is not connected then call method to connect, else do nothing
        	if (mBluetoothService.getState() != BluetoothScaleService.STATE_CONNECTED){
        	startDeviceListActivity();
        	}
        	else Toast.makeText(getApplicationContext(), "Scale Already Connected.", Toast.LENGTH_SHORT).show();
        	break;
        case R.id.menu_scale_disconnect:
        	if(D) Log.d(TAG, "+++menu_disconnect+++");
        	System.out.println("+++menu_scale_disconnect+++");
        	// TODO: if scale is connected then call method to DISconnect, else do nothing
        	if (mBluetoothService.getState() == BluetoothScaleService.STATE_CONNECTED){
        	Toast.makeText(getApplicationContext(), "Scale Disconnecting..", Toast.LENGTH_SHORT).show();
        	disconnectDevices();
        	}
        	break;
        	
        	
        case R.id.menu_scanner_connect:
        	if(D) Log.d(TAG, "+++menu_connect+++");
        	System.out.println("+++menu_scanner_connect+++");
        	// TODO: if scale is not connected then call method to connect, else do nothing
        	if (KTSyncData.mChatService.getState() != BluetoothScaleService.STATE_CONNECTED){
        	startScannerListActivity();
        	}
        	else Toast.makeText(getApplicationContext(), "Scanner Already Connected.", Toast.LENGTH_SHORT).show();
        	break;
        case R.id.menu_scanner_disconnect:
        	if(D) Log.d(TAG, "+++menu_disconnect+++");
        	System.out.println("+++menu_scanner_disconnect+++");
        	// TODO: if scale is connected then call method to DISconnect, else do nothing
        	if (KTSyncData.mChatService.getState() == BluetoothChatService.STATE_CONNECTED){
        	Toast.makeText(getApplicationContext(), "Scanner Disconnecting..", Toast.LENGTH_SHORT).show();
        	disconnectScanner();
        	}
        	break;
        case R.id.menu_auto_upload:
        	if(D) Log.d(TAG, "+++menu_auto_upload+++");
        	System.out.println("+++menu_auto_upload+++");
        	if (allEntriesUploaded()) Toast.makeText(getApplicationContext(), "All Entries Already Uploaded.", Toast.LENGTH_SHORT).show();
        	else{
        		
        		showProgress(true);
        		for (int i = 0; i< dataList.size(); i++){ // go through each entry in the list
        			if (dataList.get(i).isUploaded() != true) { // if not uploaded..
        		mAutoUploadTask = new AutoUploadTask();
        		mAutoUploadTask.execute(i);
        			}//end if
        		}// end for
        	}
            break;
        	
        	
        }
        
        return false;
    }
	
	// to check if entries have all been uploaded or not
		private boolean allEntriesUploaded() {
			boolean status = true;
			if (dataList.size() == 0) status=true; // if list is empty, they are all uploaded, so set to true
			else{
			for (int i = 0; i< dataList.size(); i++ ){ // if any item is not uploaded then set to false, break the for loop and return
				if (dataList.get(i).isUploaded() != true) {
					status = false;
					break;
				}
			}
			}
			return status;
		}
		
		private void showProgress(boolean show){
			// TODO: add fancy animation, if available
			
			
			mUploadStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			mDataView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
		
		/**
		 * Represents an asynchronous Upload task used upload entries.
		 */
		public class AutoUploadTask extends AsyncTask<Integer, Void, Integer> {
			
			@Override // can call integers[0] etc...
			protected Integer doInBackground(Integer...integers) {
				// TODO: attempt upload to a network service.
				if(D)Log.d(TAG, "+++ doInBackground +++");
				int i = (int) integers[0];
				Integer returnValue = i;
				
					//return autoUploadEntries();
				
				// if all entries upload, returns TRUE, else FALSE
				// Create a new HttpClient and Post Header
			    HttpClient httpclient = new DefaultHttpClient();
			    String URL = "http://www.sharpic.ca/usertest/entry";
			    HttpPost httppost = new HttpPost(URL);
			    //for (int i = 0; i< dataList.size(); i++){ // go through each entry in the list
			    	 // if an entry is not uploaded...
			    		try {
			    			// Add data
			    			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);// can hold 7 pairs + 1 for encrypted key
			    			//nameValuePairs.add(new BasicNameValuePair("encryptKey", encryptKey));
			    			
			    			nameValuePairs.add(new BasicNameValuePair("auditorID", dataList.get(i).getAuditorID()));
			    			nameValuePairs.add(new BasicNameValuePair("venue", dataList.get(i).getVenue()));
			    			nameValuePairs.add(new BasicNameValuePair("room", dataList.get(i).getRoom()));
			    			nameValuePairs.add(new BasicNameValuePair("weightG", dataList.get(i).getStringWeightG()));
			    			nameValuePairs.add(new BasicNameValuePair("weightOZ", dataList.get(i).getStringWeightOZ()));
			    			nameValuePairs.add(new BasicNameValuePair("barcode", dataList.get(i).getBarcode()));
			    			nameValuePairs.add(new BasicNameValuePair("time", dataList.get(i).getUnixTime()));// send the unix time 
			    			
			    			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			    			System.out.println("doInBackground:   returnValue = " + returnValue + "    int i = " + integers[0]);
			    			System.out.println(" -- data = " + nameValuePairs);
			    			// Execute HTTP Post Request
			    			long t = System.currentTimeMillis();
			    			HttpResponse response = httpclient.execute(httppost);
			    			int responseCode = response.getStatusLine().getStatusCode();
			    			
			    			// THE BELOW CODE DOES NOT EXECUTE!!! WTF (SOMETHING WITH THE HTTP EXECUTION?)
			    			
			    			
			    			//returnValue = integers[0];
			    			Log.d(TAG, "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");
			    			Log.d(TAG," -- HTTP response: " + response);
			    			
			    			// IF RESPONSE CODE IS GOOD, THEN MARK THE ENTRY AS UPLOADED!!!!
			    			//if (responseCode == 302){
			    			 //dataList.get(i).uploaded=true;
			    			 //adapter.notifyDataSetChanged();
			    			// Toast.makeText(getApplicationContext(), " -UPLOAD STATUS = "+ dataList.get(i).uploaded, Toast.LENGTH_SHORT).show();
			    			 //returnValue = integers[0];
			    			//}
			    			//else{
			    				//System.out.println(" -- ResponseCode = "+ responseCode);
			    				//Toast.makeText(getApplicationContext(), "Could not upload entry -- ResponseCode = "+ responseCode, Toast.LENGTH_SHORT).show();
			    				//returnValue = -1;
			    				
			    			//}
			    		} 	catch (ClientProtocolException e) {
			    			// TODO Auto-generated catch block
			    		} 	catch (IOException e) {
			    			// TODO Auto-generated catch block
			    		}
			    	//}// end if
			    //}// end for loop
						return returnValue;
			    
				
				
				
			
			}// end doInBackground

			
			
			// onPostExecute: invoked on the UI thread after the background computation finishes.
			// The result of the background computation is passed to this step as a parameter.
			@Override
			protected void onPostExecute(Integer index) { // passed index of uploaded entry in dataList
				mAutoUploadTask = null;
				//showProgress(false);
				
				System.out.println("onPostExecute index = "+ index);
				
				
				if (index != -1) { // mark entry as uploaded and notify adapter that data changed
					if (D) Log.d(TAG, "--- success onPostExecute... ---");
					// TODO: update uploaded status here for each entry..
					dataList.get(index).setUploaded();
					adapter.notifyDataSetChanged();
					showProgress(false);
					Toast.makeText(getApplicationContext(), "Upload Success!", Toast.LENGTH_SHORT).show();
					//Toast.makeText(getApplicationContext(), "onPostExecute item # " + index + " -UPLOAD STATUS = "+ dataList.get(index).uploaded, Toast.LENGTH_SHORT).show();
				} 
				else if (index == -1){ // failure message... Auto-retry??
					if (D) Log.d(TAG, "--- failure onPostExecute...index = "+ index );
					Toast.makeText(getApplicationContext(), "Upload Failure!", Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			protected void onCancelled() { // toast upload cancelled
				mAutoUploadTask = null;
				showProgress(false);
				Toast.makeText(getApplicationContext(), "Upload Cancelled!", Toast.LENGTH_SHORT).show();
			}
		}// end userlogin task
		
		
		private boolean autoUploadEntries(){
			// Show Progress Bar
			
			//showProgress(true);
			// Execute the upload task
			//boolean status = false;
			
			// Create a new HttpClient and Post Header
		    DefaultHttpClient httpclient = new DefaultHttpClient();
		    String URL = "http://www.sharpic.ca/usertest/entry";
		    HttpPost httppost = new HttpPost(URL);
		    for (int i = 0; i< dataList.size(); i++){
		    	if (dataList.get(i).isUploaded() != true) { // if entry not uploaded...
		    		try {
		    			// Add data
		    			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);// can hold 7 pairs + 1 for encrypted key
		    			//nameValuePairs.add(new BasicNameValuePair("encryptKey", encryptKey));
		    			
		    			nameValuePairs.add(new BasicNameValuePair("auditorID", dataList.get(i).getAuditorID()));
		    			nameValuePairs.add(new BasicNameValuePair("venue", dataList.get(i).getVenue()));
		    			nameValuePairs.add(new BasicNameValuePair("room", dataList.get(i).getRoom()));
		    			nameValuePairs.add(new BasicNameValuePair("weightG", dataList.get(i).getStringWeightG()));
		    			nameValuePairs.add(new BasicNameValuePair("weightOZ", dataList.get(i).getStringWeightOZ()));
		    			nameValuePairs.add(new BasicNameValuePair("barcode", dataList.get(i).getBarcode()));
		    			nameValuePairs.add(new BasicNameValuePair("time", dataList.get(i).getUnixTime()));// send the unix time 
		    			
		    			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    			
		    			System.out.println("Datalist i = " + i + " -- data = " + nameValuePairs);
		    			// Execute HTTP Post Request
		    			long t = System.currentTimeMillis();
		    			HttpResponse response = httpclient.execute(httppost);
		    			int responseCode = response.getStatusLine().getStatusCode();
		    			Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");
		    			System.out.println("DataList item #" + i+ " -- HTTP response: " + response);
		    			
		    			// IF RESPONSE CODE IS GOOD, THEN MARK THE ENTRY AS UPLOADED!!!!
		    			if (responseCode == 302){
		    			 dataList.get(i).uploaded=true;
		    			 adapter.notifyDataSetChanged();
		    			 Toast.makeText(getApplicationContext(), "Uploaded item # " + i + " -UPLOAD STATUS = "+ dataList.get(i).uploaded, Toast.LENGTH_SHORT).show();
		    			 
		    			}
		    			else{
		    				Toast.makeText(getApplicationContext(), "Could not upload entry # " + i+" -- ResponseCode = "+ responseCode, Toast.LENGTH_SHORT).show();
		    				return false;
		    				
		    			}
		    		} 	catch (ClientProtocolException e) {
		    			// TODO Auto-generated catch block
		    		} 	catch (IOException e) {
		    			// TODO Auto-generated catch block
		    		}
		    	}// end if
		    }// end for loop
		    // if the else case was not captured then the upload must have succeeded for all un-uploaded entries
		   return true;
		}// end method autoUplaodEntries
	
	private void startDeviceListActivity() {
		if(D) Log.d(TAG, "+++startDeviceListActivity+++");
		System.out.println("+++startDeviceListActivity+++");
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }   
	private void startScannerListActivity() {
		if(D) Log.d(TAG, "+++SCANNER ListActivity+++");
		System.out.println("+++SCANNER ListActivity+++");
        Intent serverIntent = new Intent(this, ScannerListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_SCANNER); // attempts to connect to the BT device via BTchat service
    }
	private void disconnectScanner() {
		if(D) Log.d(TAG, "+++disconnectSCANNER+++");
		System.out.println("+++disconnectSCANNER+++");
    	//stopSendBTCommand();  //just send the command to stop reading weight so that buffer in the scale is cleared before we disconnect
    	if (KTSyncData.mChatService != null) KTSyncData.mChatService.stop();
        //KTSyncData.mChatService = null;
        //KTSyncData.bIsRunning = false;
    	
    	//onBluetoothStateChanged();// changes visiblity of bottom UI toolbar items based on scale connection status
    }
	private void disconnectDevices() {
		if(D) Log.d(TAG, "+++disconnectDevices+++");
		System.out.println("+++disconnectDevices+++");
    	stopSendBTCommand();  //just send the command to stop reading weight so that buffer in the scale is cleared before we disconnect
    	if (mBluetoothService != null) mBluetoothService.stop();
    	
    	//onBluetoothStateChanged();// changes visiblity of bottom UI toolbar items based on scale connection status
    }
	/*
	 * Function: stopSendBTCommand()
	 * Description: This function stops sending any command to BT scales
	 * Called when application needs to be closed or when Bluetooth connection is about to be disconnected 
	 */
	private void stopSendBTCommand()
	{
		if(D) Log.d(TAG, "+++stopSendBTCommand+++");
		System.out.println("+++stopSendBTCommand+++");
		sendMessage(cmdStopSendCmd); //just send the command to stop reading weight so that buffer in the scale is cleared before we disconnnect
		//intCmdIndex = CMD_STOP_SEND;
	}
	
	
	
	
    
    /**
     * Sends a message.
     * @param message  A string of text to send.
     * For commands that use bytes or characters, this function can be used
     * to send command to BT scale
     */
    private void sendMessage(byte[] byteCommand) {
    	if(D) Log.d(TAG, "+++sendMessage(byte)+++");
    	System.out.println("+++sendMessage(byte)+++");
        
        
        //write/send command to BT device
        mBluetoothService.write(byteCommand);

        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
        //show the byte commands in the text field so we can verify		
        tvScaleCommand.setText("");
        String strCommand = "";
        for(int i = 0; i < byteCommand.length; i++)
        {
        	strCommand = strCommand + "0x" + String.format("%02X", byteCommand[i]) + "-"; //bytes sent to scale
        }
        tvScaleCommand.setText(strCommand); //this shows the bytes of command sent to the scale - set to invisible by default, modify setvisibility above to show
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    // NOT USED?
    private void sendMessageScanner(String message) {
        // Check that we're actually connected before trying anything
        if (KTSyncData.mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "Scanner not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            KTSyncData.mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBufferScanner.setLength(0);
            //mOutEditText.setText(mOutStringBufferScanner);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "+++onActivityResult+++ " + resultCode);
        System.out.println(" +++onActivityResult+++ " + resultCode);
        switch (requestCode) {
	        case REQUEST_CONNECT_DEVICE:
	        	if(D) Log.d(TAG, "+++onActivityResult -- REQUEST CONNECT+++");
	        	System.out.println("+++onActivityResult -- REQUEST CONNECT+++");
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                // Get the device MAC address
	                String address = data.getExtras()
	                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	                // Get the BLuetoothDevice object
	                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	                // Attempt to connect to the device
	                mBluetoothService.connect(device);
	                
	            }
	            break;
	        case REQUEST_ENABLE_BT:
	        	if(D) Log.d(TAG, "+++onActivityResult -- REQUEST ENABLE+++");
	        	System.out.println("+++onActivityResult -- REQUEST ENABLE+++");
	            // When the request to enable Bluetooth returns
	            if (resultCode == Activity.RESULT_OK) {
	                // Bluetooth is now enabled, so set up a Bluetooth session
	                setupUserInterface();
	            } else {
	                // User did not enable Bluetooth or an error occurred
	                Log.d(TAG, "BT not enabled");
	                System.out.println("BT not enabled");
	                Toast.makeText(this, "Bluetooth not enabled, quitting...", Toast.LENGTH_SHORT).show();
	                finish(); // exits the app
	            }
	            break;
	        case REQUEST_CONNECT_SCANNER:
	        	if(D) Log.d(TAG, "+++onActivityResult -- REQUEST CONNECT+++");
	        	System.out.println("+++onActivityResult -- REQUEST CONNECT - SCANNER+++");
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                // Get the device MAC address
	                String addressScanner = data.getExtras()
	                                     .getString(ScannerListActivity.EXTRA_DEVICE_ADDRESS);
	                // Get the BLuetoothDevice object
	                deviceScanner = mBluetoothAdapter.getRemoteDevice(addressScanner);
	                // Attempt to connect to the device
	                System.out.println(" REQUEST CONNECT SCANNER: BTdevice = " + deviceScanner+ " ; MAC address: " + addressScanner);
	                KTSyncData.mChatService.connect(deviceScanner);
	                //clickedRead(btnRead);
	            }
	            break;
        }
    }

    
    /*
	 * Function: SendBTCommand
	 * Parameter: Index of the command selected from the dialog box
	 * This function will send the corresponding byte command to the scale
	 * after user selects from the dialog box. 
	 */
	private void sendBTCommand(int intCmdIndex)
	{if(D) Log.d(TAG, "+++sendBTCommand+++" );
		System.out.println("+++sendBTCommand+++" + intCmdIndex);
		switch(intCmdIndex)
		{
			case CMD_READ_ONCE:
				sendMessage(cmdReadWeightOnce);
				break;
			case CMD_READ_CONT:
				sendMessage(cmdReadWeightCont);
				break;
			case CMD_STOP_SEND:
				sendMessage(cmdStopSendCmd);
				break;
			case CMD_TARE_WT:
				sendMessage(cmdTareWeight);
				break;
			case CMD_SET_GR:
				sendMessage(cmdSetUnitG);
				break;
			case CMD_SET_LBOZ:
				sendMessage(cmdSetUnitLBOZ);
				break;
			case CMD_SET_TIMER:
				intOffTimer = 0;
				//ShowOffTimerDialog();
				break;
			case CMD_READ_BATLEVEL:
				sendMessage(cmdReadBatLevel);
				break;
			case CMD_GET_CONFIG:
				sendMessage(cmdGetScaleConfig);
				break;
			case CMD_READ_SN:
				sendMessage(cmdReadSN);
				break;
			case CMD_TURN_OFF:
				sendMessage(cmdTurnOff);
				disconnectDevices();
				break;
		}
	}
    
	 @Override
	    public synchronized void onResume() {
	        super.onResume();
	        if(D) Log.e(TAG, "+ ON RESUME +");

	        // Performing this check in onResume() covers the case in which BT was
	        // not enabled during onStart(), so we were paused to enable it...
	        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
	        if (KTSyncData.mChatService != null) {
	            // Only if the state is STATE_NONE, do we know that we haven't started already
	            if (KTSyncData.mChatService.getState() == BluetoothChatService.STATE_NONE) {
	              // Start the Bluetooth chat services
	            	KTSyncData.mChatService.start();
	            }
	        }
	        KTSyncData.bIsBackground = false;

	        if ( KTSyncData.bIsConnected && KTSyncData.LockUnlock) {
	        	//Toast.makeText(this, "KTDemo Main Screen", Toast.LENGTH_LONG).show();
	        	KTSyncData.mKScan.LockUnlockScanButton(true);
	        }
	        
	        KTSyncData.mKScan.mHandler = mHandlerScanner;         
	    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
       // stopSendBTCommand(); //just send the command to stop reading weight so that buffer in the scale is cleared before we disconnnect      
        disconnectDevices();
        disconnectScanner();
        //if (mBluetoothService != null) mBluetoothService.stop();
        if(D) Log.d(TAG, "--- ON DESTROY ---");
    }
	
	
	private Runnable mUpdateTimeTask = new Runnable() {   
    	public void run() {       
    		if ( KTSyncData.AutoConnect && KTSyncData.bIsRunning )
        		KTSyncData.mChatService.connect(deviceScanner);
    	}
    };  
	
	   // The Handler that gets information back from the BluetoothChatService (Scanner)
    private final Handler mHandlerScanner = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                System.out.println("+++mHandlerScanner -- MESSAGE_STATE_CHANGE: " + msg.arg1+ "+++");
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                	System.out.println("+++mHandlerScanner -- STATE CONNECTED+++");
                    tvConnectionScanner.setText("Connected.");
                    //tvConnectionScanner.append(mConnectedScannerName);
                    //mConversationArrayAdapter.clear();
                    removeCallbacks(mUpdateTimeTask);                      
                    KTSyncData.mKScan.DeviceConnected(true);
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                	System.out.println("+++mHandlerScanner -- STATE CONNECTING+++");
                    tvConnectionScanner.setText("Connecting...");
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                	System.out.println("+++mHandlerScanner -- STATE NONE+++");
                    tvConnectionScanner.setText("Disconnected.");
                    break;
                case BluetoothChatService.STATE_LOST:
                	System.out.println("+++mHandlerScanner -- STATE LOST+++");
                    KTSyncData.bIsConnected = false;
                    tvConnectionScanner.setText("Disconnected.");
                    postDelayed(mUpdateTimeTask, 2000);       
                	break;
                case BluetoothChatService.STATE_FAILED:
                	System.out.println("+++mHandlerScanner -- STATE FAILED+++");
                	tvConnectionScanner.setText("Disconnected.");                    
                    postDelayed(mUpdateTimeTask, 5000);
                    break;                    
                }
                break;
            case MESSAGE_WRITE:
            	System.out.println("+++mHandlerScanner -- MESSAGE WRITE+++");
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                //String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
            	System.out.println("+++mHandlerScanner -- MESSAGE READ+++");
                byte[] readBuf = (byte[]) msg.obj;
                
                // construct a string from the valid bytes in the buffer
                //String readMessage = new String(readBuf, 0, msg.arg1);
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                
                for (int i = 0; i < msg.arg1; i++) KTSyncData.mKScan.HandleInputData(readBuf[i]);
                
                break;
            case MESSAGE_DEVICE_NAME:
            	System.out.println("+++mHandlerScanner -- MESSAGE DEVICE NAME+++");
                // save the connected device's name
                mConnectedScannerName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedScannerName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
            	System.out.println("+++mHandlerScanner -- MESSAGE TOAST+++");
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_DISPLAY:
            	System.out.println("+++mHandlerScanner -- MESSAGE DISPLAY+++");
                //byte[] 
                displayBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                //String 
                displayMessage = new String(displayBuf, 0, msg.arg1);
                System.out.println("+++ MESSAGE DISPLAY: displayMessage = "+ displayMessage);
                //mConversationArrayAdapter.add(displayMessage);
                addEntry(displayMessage, adapter);
                adapter.notifyDataSetChanged();
                //dispatchBarcode(displayBuf, msg.arg1);  
                KTSyncData.bIsSyncFinished = true;
            	break;            
	        case MESSAGE_SEND:
	        	System.out.println("+++mHandlerScanner -- MESSAGE SEND+++");
	        	//mConversationArrayAdapter.add(new String("1"));
                byte[] sendBuf = (byte[]) msg.obj;
                
                KTSyncData.mChatService.write(sendBuf);
	        	break;
	        case MESSAGE_SETTING:
	        	System.out.println("+++mHandlerScanner -- MESSAGE SETTING+++");
                
	        	//Intent settingsActivity = new Intent(getBaseContext(), Settings.class);
                //startActivity(settingsActivity);   
	        	break;	        	
	        }            
        }
        
		private void addEntry(String displayMessage, NickAdapter adapter) {
			System.out.println("+++add Entry +++");
				String scaleData =  txtRcvData.getText().toString(); 
				String formattedScaleData = formatScaleData(scaleData);
				if (OZflag){ // if units are set to grams
					double OZreading = Entry.string2double(formattedScaleData); // turn oz reading into double
					double Gconvert = Entry.oz2grams(OZreading); // convert to grams
					formattedScaleData = Entry.double2string(Gconvert); // save grams conversion as a string
				}
				dataList.add(new Entry(01, venue, room, formattedScaleData, displayMessage, new Date(), System.currentTimeMillis() / 1000L));		
				adapter.notifyDataSetChanged();
		}

		private String formatScaleData(String scaleData) {
			System.out.println("+++format ScaleData+++");
			// must remove the unit from the string
			// if last char is 'g', then remove the last 2 characters, 
			// if last char is 'z', then remove the last 3 characters.
			// last char is located at String.length() - 1
			String formatted = null;
			scaleData = scaleData.trim(); // trim leading and trailing white spaces
			int length = scaleData.length();
			if (scaleData.charAt(length-1) == 'g'){
				formatted = scaleData.substring(0, length-2);
			}
			else if (scaleData.charAt(length-1) == 'z'){
				formatted = scaleData.substring(0, length-3);
			}
			 
			return formatted;
		}
    };

    

	
	

	
}// end MainActivity
