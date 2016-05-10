package com.example.print;

// java date formatter
import java.text.SimpleDateFormat;
import java.util.Date;

// android sms helper
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

/* Class that inherits Broadcast Receiver */
public class SMSBroadcastReceiver extends BroadcastReceiver {
	// inherit BroadcastReceiver constructer 
	public SMSBroadcastReceiver() {
		super();
	}
	// override the method invoked when sms is received
	@Override
	public void onReceive(Context context, Intent intent) {
		// obtain list of "pdus"
		Object [] pdus= (Object[]) intent.getExtras().get("pdus");
		for(Object pdu: pdus){
			// retrieve message and related information
			SmsMessage smsMessage = SmsMessage.createFromPdu((byte [])pdu);
			String sender = smsMessage.getDisplayOriginatingAddress();
			String content = smsMessage.getMessageBody();
			long date = smsMessage.getTimestampMillis();
			Date timeDate = new Date(date);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = simpleDateFormat.format(timeDate);

			// debug use
			System.out.println("sms from:"+sender);
			System.out.println("sms content:"+content);
			System.out.println("sms time:"+time);

			// notify onReceived method in message listener 
			mMessageListener.OnReceived(content);

			// abort broadcast
			if("5556".equals(sender)){
				System.out.println(" abort ");
				abortBroadcast();
			}
		}
	}

	// static message listener
    private static MessageListener mMessageListener;
	// interface: MessageListener that requires override of onReceive method
	public interface MessageListener {
		public void OnReceived(String message);
	}
	// setter of message listener
	public void setOnReceivedMessageListener(MessageListener messageListener) {
		this.mMessageListener = messageListener;
	}
}