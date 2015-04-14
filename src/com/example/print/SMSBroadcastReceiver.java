package com.example.print;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    private static MessageListener mMessageListener;
	public SMSBroadcastReceiver() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		     Object [] pdus= (Object[]) intent.getExtras().get("pdus");
		     for(Object pdu:pdus){
		    	SmsMessage smsMessage=SmsMessage.createFromPdu((byte [])pdu);
		    	String sender=smsMessage.getDisplayOriginatingAddress();
		    	String content=smsMessage.getMessageBody();
		    	long date=smsMessage.getTimestampMillis();
		    	Date timeDate=new Date(date);
		    	SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	String time=simpleDateFormat.format(timeDate);
		    	
		    	System.out.println("sms from:"+sender);
		    	System.out.println("sms content:"+content);
		    	System.out.println("sms time:"+time);
		    	
		    	mMessageListener.OnReceived(content);
		    	
		    	
		    	if("5556".equals(sender)){
		    		System.out.println(" abort ");
		    		abortBroadcast();
		    	}
		    	
		     }
	}
	
		public interface MessageListener {
			public void OnReceived(String message);
		}

		public void setOnReceivedMessageListener(MessageListener messageListener) {
			this.mMessageListener=messageListener;
		}
}