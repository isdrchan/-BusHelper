package com.example.bushelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.bushelper.GetLocation.Location;

@SuppressLint("JavascriptInterface")
public class MainActivity extends Activity {
	
	private WebView webview;
	private String status;
	private Location location;
	private ProgressDialog pd; //进度条对话框
	private final String url = "file:///android_asset/index.html";
	
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
	    	Message msg = new Message();
	        Bundle data = new Bundle();
	        
	    	//请求数据
	    	GetLocation getLocation = new GetLocation();
	    	try {
//	    		GetTransfer gt = new GetTransfer();
//	    		gt.run("珠海", "北理工", "圆明新园");
	    		location = getLocation.run();
	    		data.putString("status", "OK");
			} catch (Exception e) {
				e.printStackTrace();
				data.putString("status", "false");
			}
			
	        msg.setData(data);
	        handler.sendMessage(msg);
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//显示progressdialog
		pd = ProgressDialog.show(this, "请稍等", "正在获取您的位置...", false);
		
		webview = (WebView) findViewById(R.id.webView);
		
		//在Runnable中做HTTP请求，以防阻塞UI线程抛NetworkOnMainThreadException
		new Thread(runnable).start();
		
		//设置webview的参数和加载本地页面
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);	//可使滚动条不占位
		webview.getSettings().setBuiltInZoomControls(false);	//隐藏左下角缩放按钮
		webview.getSettings().setSupportZoom(false);	//不允许html缩放
		webview.getSettings().setJavaScriptEnabled(true);	//必须！使webview中的html支持javascript，能够与安卓进行交互
		webview.getSettings().setUseWideViewPort(true);	//使自适应分辨率
		webview.getSettings().setLoadWithOverviewMode(true);	//使自适应分辨率
		webview.setWebViewClient(new webViewClient()); ////为WebView设置WebViewClient处理某些操作	
		webview.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);// 禁止由于内容过宽，导致横屏滚动。
		webview.addJavascriptInterface(this, "android");	//注意！使用这条语句，需在本类或onCreate方法添加注解@SuppressLint("JavascriptInterface")，和导入android.annotation.SuppressLint包，不然会报错。并且要用@JavascriptInterface注解的公有方法才能在webview中被调用
		
	}
	
	
	/**
	 * 关键就是为WebView设置WebViewClient，然后重写shouldOverrideUrlLoading方法即可。其中WebViewClient为WebView的一个辅助类，主要处理各种通知、请求事件。
	 * @author Dr.Chan
	 *
	 */
	 class webViewClient extends WebViewClient{ 
		 	/**
		 	 * 重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。 
		 	 */
		 	@Override 
		    public boolean shouldOverrideUrlLoading(WebView view, String url) { 
		        view.loadUrl(url); 
		        //如果不需要其他对点击链接事件的处理返回true，否则返回false 
		        return true; 
		    }
		 	
		 	/**
		 	 * 页面载入完成后调用
		 	 */
		 	@Override
		 	public void onPageFinished(WebView view, String url) {
		 		super.onPageFinished(view, url);
		 		if(status.equals("OK")) {
		 			webview.loadUrl("javascript:setCity('" + location.city + "','" + location.fullName + "')");
		 		}
		 		
		 		//页面加载完成后关闭progressdialog
		 		pd.dismiss();
		 	}
	 }
	 
	 /**
	 * 被javaScript调用的方法，主要将搜索关键词传到下一个Activity
	 * @param some
	 */
	 @JavascriptInterface
	 public void callDetailActivity(String city, String keyword) {
		//判断是否有网络连接
		ConnectivityManager con = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo networkinfo = con.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			// 当前网络不可用
			Toast.makeText(getApplicationContext(), "请打开手机的网络连接", Toast.LENGTH_SHORT).show();
		} else if(city.equals("") || keyword.equals("")) {
			Toast.makeText(getApplicationContext(), "请正确输入城市名称或公交线路", Toast.LENGTH_SHORT).show();
		} else {
			//去掉字符串首尾的空格  
			city = city.trim();
			keyword = keyword.trim();
			
			Intent intent = new Intent(MainActivity.this, DetailActivity.class);
			intent.putExtra("city", city);
			intent.putExtra("keyword", keyword);
			startActivity(intent);
		}
	 }
	 
	 @JavascriptInterface
	 public void callTransferActivity(String city, String here, String there) {
		//判断是否有网络连接
		ConnectivityManager con = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo networkinfo = con.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			// 当前网络不可用
			Toast.makeText(getApplicationContext(), "请打开手机的网络连接", Toast.LENGTH_SHORT).show();
		} else if(city.equals("") || here.equals("") || there.equals("")) {
			Toast.makeText(getApplicationContext(), "请正确输入城市名、所在地和目的地", Toast.LENGTH_SHORT).show();
		} else {
			//去掉字符串首尾的空格  
			city = city.trim();
			here = here.trim();
			there = there.trim();
			
			Intent intent = new Intent(MainActivity.this, TransferActivity.class);
			intent.putExtra("city", city);
			intent.putExtra("here", here);
			intent.putExtra("there", there);
			startActivity(intent);
		}
	 }
}
