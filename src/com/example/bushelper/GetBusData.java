package com.example.bushelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * 通过调用 爱帮公交api 获取公交信息
 * @author Dr.Chan
 *
 */

public class GetBusData {
	
	//爱帮appkey
	private final String appKey = "d706b1f36e6adfdb862f7f54c132390f";
	
	//请求url
	private final String url = "http://openapi.aibang.com/bus/lines";
	
	public int resultNum;
	
	//封装位置信息的结构体
	public class BusData {
		public String name;
		public String info;
		public String stats;
	}
	
	/**
	 * 
	 * @param city
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	private String getJsonFromServer(String city, String keyword) throws Exception {
		BufferedReader in = null;
		String result = null;
		
		HttpClient client = new DefaultHttpClient();
		
		HttpGet request = new HttpGet(url + "?app_key=" + appKey + "&alt=json&city=" + city + "&q=" + keyword);
        
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
	
	private ArrayList<BusData> jsonToBusData(String json) throws JSONException {
		
		//实例化ArrayList<BusData>对象
		ArrayList<BusData> busDataList = new ArrayList<BusData>();
		
		//获得result_num
		JSONObject jsonObject = new JSONObject(json);
		resultNum = jsonObject.getInt("result_num");
		
		//获得每个线路line的信息
		JSONObject lines = new JSONObject(jsonObject.getString("lines"));
		JSONArray line = new JSONArray(lines.getString("line"));
		for(int i = 0; i < line.length(); i++) {
			BusData busData = new BusData();
			JSONObject jsonObjLine = (JSONObject) line.get(i);
			
			busData.name = jsonObjLine.getString("name");
			busData.info = jsonObjLine.getString("info").replace(";", "<br/>");
			
			//改变格式，使更美观
			int j = 2;
			String temp = "(1)" + jsonObjLine.getString("stats");
			while(temp.indexOf(";") > 0) {
				temp = temp.replaceFirst(";", "<br/>(" + j +")");
				j++;
			}
			busData.stats = temp;
				
			busDataList.add(busData);
		}
		
		//测试用，遍历输出
		for(BusData busdata:busDataList) {
			Log.i("mytag", busdata.name + "---" + busdata.info +"---" + busdata.stats);
		}
		
		return busDataList;
	}
	
	public ArrayList<BusData> run(String city, String keyword) throws Exception {
		return jsonToBusData(getJsonFromServer(city, keyword));
	}
}
