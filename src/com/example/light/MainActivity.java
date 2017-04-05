package com.example.light;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.light.helper.ConnectionDetector;
import com.example.light.helper.DatabaseHelper;



@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	private static final String NAMESPACE = "http://cfins.au.tsinghua.edu.cn/";
  	private static final String URL = "http://166.111.73.6/aircontrol/MainServices.asmx";
  	private static final String METHOD_NAME1 = "Login";
  	private static final String ID= "2";
  	private static final String PASS = "cfins";
  	private Object detail;
  	private ConnectionDetector cd;
  	private String namespace = null;
  	private String url = null;
  	private String method_name = null;
  	private String soap_action = null;
  	private String id=null;
  	private String pass=null;
  	private boolean light_status=false;
  	private boolean fan_status=false;
  	private boolean cur_status=false;
  	private final int MSG_L_SUC=1;
  	private final int MSG_L_DENY=2;
  	private final int MSG_F_SUC=10;
  	private final int MSG_F_DENY=20;
  	private final int MSG_C_SUC=100;
  	private final int MSG_C_DENY=200;
  	private final int MSG_TIMEOUT=3;
  	private int status=3;
  	private Button button1;
  	private ImageView imageView1=null;
  	private Button button2;
  	private ImageView imageView2=null;
  	private Button button3;
  	private ImageView imageView3=null;
  	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imageView1=(ImageView) findViewById(R.id.imageView1);
		button1=(Button)findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Log.d("mark","connecting Light begin");
				connecting_Light();
				Log.d("mark","connecting end");
				
			}
		});
		imageView2=(ImageView) findViewById(R.id.imageView2);
		/*button2=(Button)findViewById(R.id.button2);
		button2.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Log.d("mark","connecting fan begin");
				connecting_Fan();
				Log.d("mark","connecting end");
				
			}
		});*/
		imageView3=(ImageView) findViewById(R.id.imageView3);
		/*button3=(Button)findViewById(R.id.button3);
		button3.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Log.d("mark","connecting curtain begin");
				connecting_Curtain();
				Log.d("mark","connecting end");
				
			}
		});*/
		
		DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,"ip_db");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("create table if not exists light_service(namespace varchar(100), url varchar(100), id varchar(100), password varchar(100), action1 varchar(30))");
		Log.d("mark","create table");
		Cursor cursor = db.query("light_service", null, null, null, null, null, null);
		
		if (cursor.moveToNext()){
			namespace = cursor.getString(cursor.getColumnIndex("namespace"));
			url = cursor.getString(cursor.getColumnIndex("url"));
			id = cursor.getString(cursor.getColumnIndex("id"));
			pass = cursor.getString(cursor.getColumnIndex("password"));
			method_name = cursor.getString(cursor.getColumnIndex("action1"));
			soap_action = namespace + method_name;
			Log.d("mark","read table");
		}
		else {
			Log.d("mark","put value");
			ContentValues values = new ContentValues();
			values.put("namespace", NAMESPACE);
			values.put("url", URL);
			values.put("id", ID);
			values.put("password", PASS);
			values.put("action1", METHOD_NAME1);
			Log.d("mark","insert table");
			db.insert("light_service", null, values);
			namespace = NAMESPACE;
			url = URL;
			method_name = METHOD_NAME1;
			id=ID;
			pass=PASS;
			soap_action = namespace + method_name;
			
		}
		db.close();
		dbHelper.close();
		
/////////////////////////////////////////////////////////////////////////////////
		soap_action = NAMESPACE + METHOD_NAME1;
		cd = new ConnectionDetector(getApplicationContext());
		if (cd.isConnectingToInternet() == false){
			Toast.makeText(MainActivity.this, "请打开网络连接 :(", Toast.LENGTH_LONG).show();
			return;
		}
	}
	final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Log.d("mark","msg rec");
			switch(msg.what){
			case MSG_L_SUC:
				Log.d("mark","MSG_L_SUC");
				if(light_status==true)
					imageView1.setImageResource(R.drawable.light_on);
					else imageView1.setImageResource(R.drawable.light_off);
				Toast.makeText(MainActivity.this, "灯光操作成功 :）", Toast.LENGTH_LONG).show();
				break;
			case MSG_L_DENY:
				Log.d("mark","MSG_L_DENY");
				Toast.makeText(MainActivity.this, "灯光操作失败 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_F_SUC:
				Log.d("mark","MSG_F_SUC");
				if(fan_status==true)
					imageView2.setImageResource(R.drawable.fan_on);
					else imageView2.setImageResource(R.drawable.fan_off);
				Toast.makeText(MainActivity.this, "风扇操作成功 :）", Toast.LENGTH_LONG).show();
				break;
			case MSG_F_DENY:
				Log.d("mark","MSG_F_DENY");
				Toast.makeText(MainActivity.this, "风扇操作失败 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_C_SUC:
				Log.d("mark","MSG_C_SUC");
				if(cur_status==true)
					imageView3.setImageResource(R.drawable.curtain_on);
					else imageView3.setImageResource(R.drawable.curtain_off);
				Toast.makeText(MainActivity.this, "窗帘操作成功 :）", Toast.LENGTH_LONG).show();
				break;
			case MSG_C_DENY:
				Log.d("mark","MSG_C_DENY");
				Toast.makeText(MainActivity.this, "窗帘操作失败 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_TIMEOUT:
				Log.d("mark","MSG_TIMEOUT");
				Toast.makeText(MainActivity.this, "服务器不存在或连接超时 :(", Toast.LENGTH_LONG).show();
				break;
			case 0:
				Toast.makeText(MainActivity.this, "服务器出错啦！赶快调! @.@", Toast.LENGTH_LONG).show();
			}
			super.handleMessage(msg);
			
			
		}
	};

	private void connecting_Fan(){
		Thread thread=new Thread(){
		public void run(){
		//////////////////////////////////////////////////////////////////
			DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,"ip_db");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Log.d("mark","query table");
			Cursor cursor = db.query("light_service", null, null, null, null, null, null);
			if (cursor.moveToNext()){
				namespace = cursor.getString(cursor.getColumnIndex("namespace"));
				url = cursor.getString(cursor.getColumnIndex("url"));
				id = cursor.getString(cursor.getColumnIndex("id"));
				pass = cursor.getString(cursor.getColumnIndex("password"));
				method_name = cursor.getString(cursor.getColumnIndex("action1"));
				
				Log.d("mark","read table");
			}
			else Log.d("mark","query failed");
			
			db.close();
			dbHelper.close();
		//////////////////////////////////////////////////////////////////
		org.ksoap2.transport.HttpTransportSE ht=null;
		try {
		ht=	new HttpTransportSE(url,6000);
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		ht.debug = true;
		soap_action = namespace + "WindControl";
		SoapObject rpc = new SoapObject(namespace, "WindControl");
		rpc.addProperty("username", id);
		rpc.addProperty("password", pass);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = rpc;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(rpc);
		
		try{
		ht.call(soap_action, envelope);
		detail =(Object) envelope.getResponse();
		String result = detail.toString();
		System.out.println(result);
		if (result.startsWith("Wind")){
			Log.d("mark","access suc");
			fan_status=!fan_status;
			status=MSG_F_SUC;
			SendMSG();
			
		}
		else if (result.startsWith("Failed")){
			status=MSG_F_DENY;
			Log.d("mark","access denied");
			SendMSG();
		}
		else{
			status=0;
			SendMSG();
		}
		
		return;
		}
	
	catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		status=MSG_TIMEOUT;
		Log.d("mark","exception");
		SendMSG();
		}
		
	}
	};
		thread.start();
		thread=null;
			
	}
	
	private void connecting_Curtain(){
		Thread thread=new Thread(){
		public void run(){
			
		//////////////////////////////////////////////////////////////////
			DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,"ip_db");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Log.d("mark","query table");
			Cursor cursor = db.query("light_service", null, null, null, null, null, null);
			if (cursor.moveToNext()){
				namespace = cursor.getString(cursor.getColumnIndex("namespace"));
				url = cursor.getString(cursor.getColumnIndex("url"));
				id = cursor.getString(cursor.getColumnIndex("id"));
				pass = cursor.getString(cursor.getColumnIndex("password"));
				method_name = cursor.getString(cursor.getColumnIndex("action1"));
				
				Log.d("mark","read table");
			}
			else Log.d("mark","query failed");
			
			db.close();
			dbHelper.close();
		//////////////////////////////////////////////////////////////////
		org.ksoap2.transport.HttpTransportSE ht=null;
		try {
		ht=	new HttpTransportSE(url,6000);
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		ht.debug = true;
		soap_action = namespace + "BlindControl";
		SoapObject rpc = new SoapObject(namespace,"BlindControl");
		rpc.addProperty("username", id);
		rpc.addProperty("password", pass);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = rpc;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(rpc);
		
		try{
		ht.call(soap_action, envelope);
		detail =(Object) envelope.getResponse();
		String result = detail.toString();
		System.out.println(result);
		if (result.startsWith("Blind")){
			Log.d("mark","access suc");
			cur_status=!cur_status;
			status=MSG_C_SUC;
			SendMSG();
			
		}
		else if (result.startsWith("Failed")){
			status=MSG_C_DENY;
			Log.d("mark","access denied");
			SendMSG();
		}
		else{
			status=0;
			SendMSG();
		}
		
		return;
		}
	
	catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		status=MSG_TIMEOUT;
		Log.d("mark","exception");
		SendMSG();
		}
		
	}
	};
		thread.start();
		thread=null;
			
	}
	
	private void connecting_Light(){
		Thread thread=new Thread(){
		public void run(){
		//////////////////////////////////////////////////////////////////
			DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,"ip_db");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Log.d("mark","query table");
			Cursor cursor = db.query("light_service", null, null, null, null, null, null);
			if (cursor.moveToNext()){
				namespace = cursor.getString(cursor.getColumnIndex("namespace"));
				url = cursor.getString(cursor.getColumnIndex("url"));
				id = cursor.getString(cursor.getColumnIndex("id"));
				pass = cursor.getString(cursor.getColumnIndex("password"));
				method_name = cursor.getString(cursor.getColumnIndex("action1"));
				
				Log.d("mark","read table");
			}
			else Log.d("mark","query failed");
			
			db.close();
			dbHelper.close();
		//////////////////////////////////////////////////////////////////
		org.ksoap2.transport.HttpTransportSE ht=null;
		try {
		ht=	new HttpTransportSE(url,6000);
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		ht.debug = true;
		soap_action = namespace + "LightControl";
		SoapObject rpc = new SoapObject(namespace, "LightControl");
		rpc.addProperty("username", id);
		rpc.addProperty("password", pass);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = rpc;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(rpc);
		
		try{
		ht.call(soap_action, envelope);
		detail =(Object) envelope.getResponse();
		String result = detail.toString();
		System.out.println(result);
		if (result.startsWith("Light")){
			Log.d("mark","access suc");
			light_status=!light_status;
			status=MSG_L_SUC;
			SendMSG();
			
		}
		else if (result.startsWith("Failed")){
			status=MSG_L_DENY;
			Log.d("mark","access denied");
			SendMSG();
		}
		else{
			status=0;
			SendMSG();
		}
		
		return;
		}
	
	catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		status=MSG_TIMEOUT;
		Log.d("mark","exception");
		SendMSG();
		}
		
	}
	};
		thread.start();
		thread=null;
			
	}
	private void SendMSG(){
		Log.d("mark","msg sent");
		Message message=new Message();
		message.what=status;
		handler.sendMessage(message);
		System.out.println("send");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, "配置");
		// Inflate the menu; this adds items to the action bar if it is present.
		return super.onCreateOptionsMenu(menu);
    }
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == 1){
			Intent intent = new Intent(this,Configuration.class);
			startActivity(intent);	
		}
		return super.onOptionsItemSelected(item);
	}
}

