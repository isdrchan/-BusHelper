package com.example.bushelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * 通过调用 百度ip定位 api获取城市
 * @author Dr.Chan
 *
 */
public class GetLocation {
	
	//百度api的ak
	private final String appKey = "08f6ef39dd1e939ab7438b5847842dbc";
	
	//请求url
	private final String url = "http://api.map.baidu.com/location/ip";
	
	//封装位置信息的结构体
	public class Location {
		public String city;
		public String fullName;
		public String x;
		public String y;
	}
	
	/**
	 * 请求数据
	 */
	private String getJsonFromServer() throws Exception{
		BufferedReader in = null;
		String result = null;
		
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		
        // 创建名/值组列表  
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("ak", appKey));
        parameters.add(new BasicNameValuePair("coor", "bd09ll"));
        
        // 创建UrlEncodedFormEntity对象  
        UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);//设置编码，防止中文乱码 
        request.setEntity(formEntiry);
        
        // 执行请求  
        HttpResponse response = client.execute(request);
        
        // 接收并处理返回数据
        in = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
        StringBuffer sb = new StringBuffer("");
        String line = "";
        String NL = System.getProperty("line.separator");
        while ((line = in.readLine()) != null) {  
            sb.append(line + NL);
        } 
        in.close(); 
        result = sb.toString();

        //测试用，logcat打印返回的json字符串
        Log.i("myTag", "服务器返回的结果：" + result);
		
		return result;
	}
	
	/**
	 * 
	 */
	private Location jsonToLocation(String json) throws JSONException {
		
		//实例化Location对象
		Location location = new Location();
		
		//获得status
		JSONObject jsonObject = new JSONObject(json);
		int status = jsonObject.getInt("status");
		
		//status为0再解析
		if(status == 0) {
			//获得城市
			String address = jsonObject.getString("address");
			String addressArray[] = address.split("\\|");
			location.city = addressArray[2];
			
			//获得城市全称
			JSONObject content = new JSONObject(jsonObject.getString("content"));
			location.fullName = content.getString("address");
			
			//获得x，y坐标
			JSONObject point = new JSONObject(content.getString("point"));
			location.x = point.getString("x");
			location.y = point.getString("y");
		}
		
		//Debug
		Log.i("location", location.city + "---" + location.fullName + "---" + location.x + "---" + location.y);
		
		return location;
	}
	
	public Location run() throws Exception{
		return jsonToLocation(getJsonFromServer());
	}
	
	/* 返回示例
	 {
   "address": "CN|广东|珠海|None|UNICOM|0|0",
   "content": {
       "address_detail": {
           "province": "广东省",
           "city": "珠海市",
           "district": "",
           "street": "",
           "street_number": "",
           "city_code": 140
       },
       "address": "广东省珠海市",
       "point": {
           "y": "2526069.55",
           "x": "12641851.33"
       }
   },
   "status": 0
	} 
	 */
}
