package com.example.print;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.example.print.SMSBroadcastReceiver.MessageListener;


public class MainActivity extends Activity{
	
	private WifiManager wifiManager;
	private List<ScanResult> list;
	private List<WifiConfiguration> ConfList;
	private SimpleDateFormat sDateFormat;
	private TextView textView;
	private TextView textView1;
	private ArrayList<String> ssidList = new ArrayList<String>();
	private ArrayList<String> bssidList = new ArrayList<String>();
	private String companyName;
	private String printerIpAddres = "192.168.223.1";
	private boolean isPrinter = false;
	private boolean isPrinted = false;
	private boolean reset_signal = false;
	private SMSBroadcastReceiver mSMSBroadcastReceiver;
	private String sms = "";
	private boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sDateFormat =  new SimpleDateFormat("MM-dd hh:mm:ss");

        textView = (TextView) findViewById(R.id.textview);
        textView1 = (TextView) findViewById(R.id.textview1);
        
        mSMSBroadcastReceiver = new SMSBroadcastReceiver();
		mSMSBroadcastReceiver.setOnReceivedMessageListener(new MessageListener() {
					@Override
					public void OnReceived(String message) {
						reset_signal = true;
						sms = message;
					}
		});
        
        
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				ssidList.clear();
				isPrinter = isPrinter();
				textView.setText(isPrinter+"");
				if(isPrinter){
					companyName = longestCommonSubstringInList(ssidList);
					textView1.setText(companyName);
					Log.i("cpppp", "print job here");
					printDocument(companyName);					
				}
				handler.post(this);
			}
		};
		
		handler.postDelayed(runnable, 3000);

    }
    
    /*To check if there's a new printer */
    
	private boolean isPrinter(){
    	boolean is = false;
    	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		list = wifiManager.getScanResults();
		ConfList = wifiManager.getConfiguredNetworks();
		if (list == null) {
			return is;
		}else {
			for (int i = 0; i<list.size();i++){
				ScanResult scanResult = list.get(i);
				String ssid = scanResult.SSID;
//				String ssid = ConfList.get(i).SSID;
				String bssid = scanResult.BSSID;
//				String bssid = ConfList.get(i).BSSID;
				
				String cap = scanResult.capabilities;
				ssidList.add(ssid);
				if((ssid.toLowerCase().contains("print")||ssid.toLowerCase().contains("hp")||ssid.toLowerCase().contains("canon"))&&(!cap.contains("WPA"))){
					if (reset_signal){
						bssidList = new ArrayList<String>();
						reset_signal = false;
					}
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
						return true;
					}
				}
			}
		}
		return false;
    }
	
    
	private static String longestCommonSubstringInList(ArrayList<String> values)
	{
		//System.out.println(values.size());
	    String result = "";
	    int largest = 0;
	    
	    ArrayList<Integer> countList = new ArrayList<Integer>();
		ArrayList<String> commonStringList = new ArrayList<String>();
	    for (int i = 0; i < values.size() - 1; i++){
	        for (int j = i + 1; j < values.size(); j++){
	            String tmp = longestCommonSubstring(values.get(i), values.get(j));
	            //Log.e("JKLASJ", tmp);
	            tmp = trim(tmp);
	            if (tmp!=null){
	            	if(!commonStringList.contains(tmp)){
	            		commonStringList.add(tmp);
	            		countList.add(1);
	            	} else {
	            		int index = commonStringList.indexOf(tmp);
	            		countList.set(index,countList.get(index)+1);
	            	}
	            }
	        }
	    }
	    
	    int current;

	    for(int i=0;i<commonStringList.size();i++){
	    	current = countList.get(i);
	    	if(current>largest){
	    		largest = current;
	    		result= commonStringList.get(i);
	    	}
	    }	
	    return result;

	}
    
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
	
	private static String trim(String s){
		s = s.replaceAll("[^a-zA-Z]", "");
		return s;
	}
	
	public void getFile(){
		File file = new File(Environment.getExternalStorageState());
	}
	
	public void printDocument(String s){
		new PrintTask().execute(new String[]{s});	    
	}
	
	class PrintTask extends AsyncTask<String, Void, Void> {
		protected void onPostExecute() {}

		@Override
		protected Void doInBackground(String... arg0) {
			try {
			Socket sock = new Socket(printerIpAddres, 9100);
			PrintWriter oStream = new PrintWriter(sock.getOutputStream());
			
			OutputStream outputStream = sock.getOutputStream();	   
			if(sms.contains("drone") | sms.equals("")){
	    		File myFile =new File("/storage/emulated/0/Download/IMG_7254.jpg");
				FileInputStream fis= new FileInputStream(myFile);
				byte[] buffer = new byte[500];
				int count = 0;
				
				while((count=fis.read(buffer,0,buffer.length))!=-1){
				outputStream.write(buffer,0,count);
				}
				outputStream.flush();	
	    	}
	    	else {
				oStream.println("Hi! :) "+arg0[0]+": "+sms);
				oStream.println("\n\n\n");
			}	

			oStream.close();
			sock.close();
			} catch (UnknownHostException e) {
			e.printStackTrace();
			} catch (IOException e) {
			e.printStackTrace();
			}
			return null;
		}
		}
}
