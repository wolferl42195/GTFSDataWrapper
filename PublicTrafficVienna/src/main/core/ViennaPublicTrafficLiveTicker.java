package core;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;




public class ViennaPublicTrafficLiveTicker 
{

	private String REQUEST_URL_Single = "http://www.wienerlinien.at/ogd_realtime/monitor?rbl=%d&sender=Aq5inVKiQsJwRm9c";
	private String REQUEST_URL_All = "http://www.wienerlinien.at/ogd_realtime/monitor?%s&sender=Aq5inVKiQsJwRm9c";
	
	public static void main(String[] args) 
	{
		long startTime = System.currentTimeMillis();
		
	    ViennaPublicTrafficLiveTicker ticker = new ViennaPublicTrafficLiveTicker();

//	    ticker.runSingle(115);
	    ticker.runAll(0, 8499);
				
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Total elapsed http request/response time in milliseconds: " + elapsedTime);

	}

	@SuppressWarnings("deprecation")
	private void runAll(int start, int end)
	{
		try 
		{
			List<String> responseJsonMessagelist = loadRealtimeData_all(start,end);
			
			for (int i = 0; i < responseJsonMessagelist.size(); ++i) 
			{
			
			String responseJsonMessage = responseJsonMessagelist.get(i);
			JSONObject responseJsonObject = new JSONObject(responseJsonMessage);
			JSONObject message = responseJsonObject.getJSONObject("message");
			//MetaData of the request
			String messageValue = (String) message.get("value");
			Integer messageCode  = (Integer) message.get("messageCode");
			String messageServerTime  = (String) message.get("serverTime");
			System.out.println("meta data of the request value="+messageValue+"; messageCode="+messageCode+", messageServerTime="+messageServerTime);
			
			JSONObject data = responseJsonObject.getJSONObject("data");
			JSONArray monitorsDetails = (JSONArray) data.get("monitors");
			
			for (int j = 0; j < monitorsDetails.length(); ++j) 
			{
			    JSONObject monitor_single = monitorsDetails.getJSONObject(j);
			    monitor_single.put("serverTime", messageServerTime);
			    
			    
			    Mongo mongo = new Mongo("localhost", 27017);
				DB db = mongo.getDB("Wiener_Linien");
				DBCollection collection = db.getCollection("DATA");

				// convert JSON to DBObject directly
				DBObject dbObject = (DBObject) JSON.parse(monitor_single.toString());

				collection.insert(dbObject);
				
			}
			
			
			
			
//			try (FileWriter file = new FileWriter("C:/Users/Raphael/Desktop/FH/Master/Data/Datafile"+i+".txt")) 
//			{
//				file.write(responseJsonObject.toString());
//				System.out.println("Successfully Copied JSON Object to File...");
//				
//			} 
//			catch (Exception e) 
//			{
//				e.printStackTrace();
//			}
			
//			System.out.println(data);

//			JSONArray monitors = data.getJSONArray("monitors");
//			
//			for (int j = 0; j < monitors.length(); ++j) 
//			{
//			    JSONObject rec = monitors.getJSONObject(j);
//			    JSONObject locationStopDetails = rec.getJSONObject("locationStop");
//			    System.out.println("geometry -->"+locationStopDetails.get("geometry"));
//			    
//			    JSONArray jsonLines = rec.getJSONArray("lines");
//			    for (int k = 0; k < jsonLines.length(); ++k) 
//			    {
//			    	String jsonLineName = (String)jsonLines.getJSONObject(k).get("name");
//			    	System.out.println("lineName-->"+jsonLineName);
//			    }
//			}
					
			}
		}
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	private List<String> loadRealtimeData_all(int start, int end) throws MalformedURLException, IOException, ProtocolException 
	{
		List<String> finalUrllist = buildURL_all(start,end);
		List<String> JSONresponselist = new ArrayList<String>();
		
		
		for (int i = 0; i < finalUrllist.size(); i++) 
		{
		String finalUrl = finalUrllist.get(i);
		URL obj = new URL(finalUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + finalUrl);
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();
			
		    JSONresponselist.add(response.toString());	
			
		}
		
		return JSONresponselist;
	}
	
	
	private List<String> buildURL_all(int start, int end) 
	{	
		List<String> URLarray = new ArrayList<String>();
		System.out.println("Requesting rbl numbers " + start + " through " + end);
	
		for(;start-1<end;)
		{
			String rbltext = String.format("rbl=%d", start);
		for (int i=1;start<end&&i<500;i++) 
		{   
			start++;
			rbltext = rbltext + "&rbl=" + start;
		}
		String finalURL = String.format(REQUEST_URL_All, rbltext);
		URLarray.add(finalURL);
		start++;
		}
		
		return URLarray;
	}
	@SuppressWarnings("deprecation")
	private void runSingle(Integer rbl) 
	{
		try 
		{
			String responseJsonMessage = loadRealtimeData(rbl);
			
			JSONObject responseJsonObject = new JSONObject(responseJsonMessage);
			JSONObject message = responseJsonObject.getJSONObject("message");
			//MetaData of the request
			String messageValue = (String) message.get("value");
			Integer messageCode  = (Integer) message.get("messageCode");
			String messageServerTime  = (String) message.get("serverTime");
			System.out.println("meta data of the request value="+messageValue+"; messageCode="+messageCode+", messageServerTime="+messageServerTime);
			
			JSONObject data = responseJsonObject.getJSONObject("data");
			/*
			System.out.println(data);
			Iterator<String> itr = message.keys();
			while(itr.hasNext()) {
				Object element = itr.next();
				System.out.println("message --> "+element);
			}
			itr = data.keys();
			while(itr.hasNext()) {
				Object element = itr.next();
				System.out.println("data --> "+element);
			}
			System.out.println("");
			System.out.println("");
			*/
			
			JSONArray monitorsDetails = (JSONArray) data.get("monitors");
			for (int i = 0; i < monitorsDetails.length(); ++i) 
			{
			    JSONObject monitor_single = monitorsDetails.getJSONObject(i);
			    monitor_single.put("serverTime", messageServerTime);
			    
			    
			    Mongo mongo = new Mongo("localhost", 27017);
				DB db = mongo.getDB("Wiener_Linien");
				DBCollection collection = db.getCollection("DATA");

				// convert JSON to DBObject directly
				DBObject dbObject = (DBObject) JSON.parse(monitor_single.toString());

				collection.insert(dbObject);
				
				
			    
//			    JSONObject locationStopDetails = monitor_single.getJSONObject("locationStop");
//			    System.out.println("geometry -->"+locationStopDetails.get("geometry"));
			    
//			    JSONArray jsonLines = monitor_single.getJSONArray("lines");
//			    for (int j = 0; j < jsonLines.length(); ++j) 
//			    {
//			    	String jsonLineName = (String)jsonLines.getJSONObject(j).get("name");
//			    	System.out.println("lineName-->"+jsonLineName);
//			    }
			}
			
		
			} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private String loadRealtimeData(Integer rbl) throws MalformedURLException, IOException, ProtocolException 
	{
		
		String finalUrl = buildURL(rbl);	
		
		URL obj = new URL(finalUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + finalUrl);
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}


	protected String buildURL(Integer rbl) 
	{
		String  finalUrl = String.format(REQUEST_URL_Single, rbl);
		return finalUrl;
	}

	
	
}