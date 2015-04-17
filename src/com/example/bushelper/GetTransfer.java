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
 * 通过调用 爱帮公交api 获取换乘信息
 * @author Dr.Chan
 *
 */

public class GetTransfer {
	//爱帮appkey
	private final String appKey = "d706b1f36e6adfdb862f7f54c132390f";
		
	//请求url
	private final String url = "http://openapi.aibang.com/bus/transfer";
	
	public int resultNum;
	
	//封装换乘信息的结构体
	public class Transfer {
		public int dist;	//总距离
		public int time;	//估计耗费时间
		public int foot_dist;	//总步行距离
		public int last_foot_dist;	//从终点站走到终点的距离
		public int transTimes; //换乘次数
		public ArrayList<Segment> segmentList = new ArrayList<Segment>(); //换乘的公交信息
	}
	public class Segment {
		public String startStat;	//起点站名
		public String endStat;	//终点站名
		public String lineName; //线路名称
		public String stats; //沿途站点
		public int lineDist;	//行驶距离
		public int footDist;	//步行距离
	}
	
	private String getJsonFromServer(String city, String here, String there) throws Exception {
		BufferedReader in = null;
		String result = null;
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url + "?app_key=" + appKey + "&alt=json&city=" + city + "&start_addr=" + here + "&end_addr=" + there);
		
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
	
	private ArrayList<Transfer> jsonToBusData(String json) throws JSONException {
		
		//实例化ArrayList<BusData>对象
		ArrayList<Transfer> transferList = new ArrayList<Transfer>();
		
		//获得result_num
		JSONObject jsonObject = new JSONObject(json);
		resultNum = jsonObject.getInt("result_num");
		
		//获得每个换乘信息
		JSONObject buses = new JSONObject(jsonObject.getString("buses"));
		JSONArray bus = new JSONArray(buses.getString("bus"));
		for(int i = 0; i < bus.length(); i++) {
			Transfer transfer = new Transfer();
			JSONObject busObject = (JSONObject) bus.get(i);
			JSONObject segments = new JSONObject(busObject.getString("segments"));
			JSONArray segmentArr = new JSONArray(segments.getString("segment"));
			
			transfer.dist = busObject.getInt("dist");
			System.out.println(transfer.dist + "sdfsdf");
			transfer.time = busObject.getInt("time");
			transfer.foot_dist = busObject.getInt("foot_dist");
			transfer.last_foot_dist = busObject.getInt("last_foot_dist");
			transfer.transTimes = segmentArr.length();
			
			for(int j = 0; j < segmentArr.length(); j++) {
				Segment segment = new Segment();
				JSONObject segmentObj = (JSONObject) segmentArr.get(j);
				
				segment.startStat = segmentObj.getString("start_stat");
				segment.endStat = segmentObj.getString("end_stat");
				segment.lineName = segmentObj.getString("line_name");
				segment.stats = segmentObj.getString("stats");
				segment.lineDist = segmentObj.getInt("line_dist");
				segment.footDist = segmentObj.getInt("foot_dist");
				
				transfer.segmentList.add(segment);
			}
			transferList.add(transfer);
		}
		
		//测试用，遍历输出
		for(Transfer transfer:transferList) {
			Log.i("mytag", transfer.dist + "----" + transfer.foot_dist +"----" + transfer.last_foot_dist);
		}
		
		return transferList;
	}
	
	public ArrayList<Transfer> run(String city, String here, String there) throws Exception {
		return jsonToBusData(getJsonFromServer(city, here, there));
	}
}
