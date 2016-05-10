package com.example.print;

// transimission usage 
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

// data structure and widget 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

// Android widget, activity and view
import android.widget.TextView;
import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

// Android wifi usage
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

// Android log 
import android.util.Log;

/* SMS listener: MessageListener class */
import com.example.print.SMSBroadcastReceiver.MessageListener;

/* Activity that detects free printer wifi and deliver printing jobs 
that are received from SMS */
public class MainActivity extends Activity{

	// Android wifi usage
	private WifiManager wifiManager;
	private List<ScanResult> list;
	private List<WifiConfiguration> ConfList;
	private SimpleDateFormat sDateFormat;
	private ArrayList<String> ssidList = new ArrayList<String>();
	private ArrayList<String> bssidList = new ArrayList<String>();
	
	// Android widget and view
	private TextView textView;
	private TextView textView1;

	/* SMS listener: MessageListener class */
	private SMSBroadcastReceiver mSMSBroadcastReceiver;

	// global container
	private String sms = "";
	private String companyName;
	private String printerIpAddres = "192.168.223.1";

	// global flag
	private boolean firstTime = true;
	private boolean isPrinter = false;
	private boolean isPrinted = false;
	private boolean reset_signal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*********************
        **** preparation *****
        **********************/

        // date formatter
        sDateFormat =  new SimpleDateFormat("MM-dd hh:mm:ss");
        // find views
        textView = (TextView) findViewById(R.id.textview);
        textView1 = (TextView) findViewById(R.id.textview1);

        // instantiate MessageListener class
        mSMSBroadcastReceiver = new SMSBroadcastReceiver();
        // store corresponding information and signal on receiving SMS
		mSMSBroadcastReceiver.setOnReceivedMessageListener( new MessageListener() {
			@Override
			public void OnReceived(String message) {
				reset_signal = true;
				sms = message;
			}
		});
        
        /*********************
        *** start detection **
        **********************/

        // THREAD: loop to check availability of free printer wifi
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				// clear the container that holds the ssid of all WIFIs
				ssidList.clear();
				// check if there is a detection of new free printer
				isPrinter = isPrinter();
				textView.setText(isPrinter+"");
				// once new free printer wifi detected 
				if(isPrinter){
					// conventionally retrieve company name as the longest common substring of all wifi ssid
					companyName = longestCommonSubstringInList(ssidList);
					textView1.setText(companyName);
					// send printing task to desired target
					printDocument(companyName);					
				}
				handler.post(this);
			}
		};
		// start looping at an interval of 3 sec
		handler.postDelayed(runnable, 3000);

    }
    
    /* Method to check if there is a detection of new free printer */
	private boolean isPrinter(){
    	boolean is = false;
    	// instantiate Android wifiManager
    	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	// obtain list of wifi information and network configuration 
		list = wifiManager.getScanResults();
		ConfList = wifiManager.getConfiguredNetworks();

		// perform below if any wifi is scanned
		if (list != null){
			// check the availability of scanned wifi one by one
			for (int i = 0; i<list.size();i++){

				//retrieve the ssid and bssid of certain scanned wifi
				ScanResult scanResult = list.get(i);
				String ssid = scanResult.SSID;
				String bssid = scanResult.BSSID;
				String cap = scanResult.capabilities;
				ssidList.add(ssid);

				//check if certain wifi is within our interest
				if((ssid.toLowerCase().contains("print")||
					ssid.toLowerCase().contains("hp")||
					ssid.toLowerCase().contains("canon"))
					&&
					(!cap.contains("WPA"))){
					// reset everything if signaled
					if (reset_signal){
						bssidList = new ArrayList<String>();
						reset_signal = false;
					}
					// add bssid to container if the wifi is of our interest
					if(!bssidList.contains(bssid)){
						bssidList.add(bssid);					
//						wifiManager.disconnect();	
//						for (WifiConfiguration cf : ConfList){
//							Log.i("cp3", cf.SSID);
//							if (cf.SSID.contains(ssid)){
//								Log.i("cp4", cf.SSID);
//								wifiManager.enableNetwork(cf.networkId, true);
//								break;
//							}
//								
//						}					
//						wifiManager.reconnect();	
						// notify detection of free printer			
						return true;
					}
				}
			}
		}
		// notify no detection of free printer	
		return false;
    }
	
    /* Snippet to find longest common substring in an Arraylist */
	private static String longestCommonSubstringInList(ArrayList<String> values)
	{
	    String result = "";
	    int largest = 0;
	    
	    ArrayList<Integer> countList = new ArrayList<Integer>();
		ArrayList<String> commonStringList = new ArrayList<String>();
	    for (int i = 0; i < values.size() - 1; i++){
	        for (int j = i + 1; j < values.size(); j++){
	            String tmp = longestCommonSubstring(values.get(i), values.get(j));
	            //Log.e("JKLASJ", tmp);
	            tmp = trim(tmp);
	            if (tmp != null){
	            	if(!commonStringList.contains(tmp)){
	            		commonStringList.add(tmp);
	            		countList.add(1);
	            	} else {
	            		int index = commonStringList.indexOf(tmp);
	            		countList.set(index,countList.get(index) + 1 );
	            	}
	            }
	        }
	    }
	    
	    int current;

	    for( int i=0; i<commonStringList.size(); i++){
	    	current = countList.get(i);
	    	if(current > largest){
	    		largest = current;
	    		result = commonStringList.get(i);
	    	}
	    }	
	    return result;

	}
    
    /* Snippet to find longest common substring */
	private static String longestCommonSubstring(String S1, String S2)
	{
	    int Start = 0;
	    int Max = 0;
	    for (int i = 0; i < S1.length(); i++){
	        for (int j = 0; j < S2.length(); j++){
	            int x = 0;
	            while (S1.charAt(i + x) == S2.charAt(j + x)){
	                x++;
	                if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) 
	                	break;}
	            if (x > Max){
	                Max = x;
	                Start = i;
	            }
	         }
	    }
	    return S1.substring(Start, (Start + Max));
	}
	
	/* Method to remove unwanted character in a string */
	private static String trim(String s){
		s = s.replaceAll("[^a-zA-Z]", "");
		return s;
	}
	
	/* Method to get file path*/
	public void getFile(){
		File file = new File(Environment.getExternalStorageState());
	}
	
	/* Method to execute printing task*/
	public void printDocument(String s){
		new PrintTask().execute(new String[]{s});	    
	}
	
	/* Async dlivering printing job to connected printer */
	class PrintTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... arg0) {
			try {
				// configure socket connection
				Socket sock = new Socket(printerIpAddres, 9100);
				// prepare stream to printer
				PrintWriter oStream = new PrintWriter( sock.getOutputStream() );
				OutputStream outputStream = sock.getOutputStream();
				// seperate type of printing task: 
				// 1. printing drone image with information
				if(sms.contains("drone") | sms.equals("")){
					// find the file in the path
					File myFile = new File("/storage/emulated/0/Download/IMG_7254.jpg");
					// prepare stream and buffer object
					FileInputStream fis = new FileInputStream(myFile);
					byte[] buffer = new byte[500];
					int count = 0;
					// deliver bytes of image to printing
					while((count = fis.read(buffer, 0, buffer.length)) != -1){
						outputStream.write(buffer, 0, count);
					}
					// send it out in output stream
					outputStream.flush();	
				}
				// 2. printing sms message received
				else {
					// deliver bytes of text message to printing
					oStream.println( "Hi! :) " + arg0[0] + ": " + sms );
					oStream.println( "\n\n\n" );
				}
				// shut down the print writer
				oStream.close();
				// close socket connection 
				sock.close();
			} catch ( UnknownHostException e ) {
				e.printStackTrace();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
