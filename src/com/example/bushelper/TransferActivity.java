package com.example.bushelper;

import java.util.ArrayList;

import com.example.bushelper.GetTransfer.Transfer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

public class TransferActivity extends Activity {
	
	private WebView webview;
	private String status;
	ArrayList<Transfer> transferList;
	private final String url = "";
	//创建Handler对象
	Handler handler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        Bundle data = msg.getData();
	        status = data.getString("status");
	        webview.loadUrl(url);
	    }
	};
	//新建一个线程对象
	Runnable runnable = new Runnable() {
	    @Override
	    public void run() {
	    	
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
