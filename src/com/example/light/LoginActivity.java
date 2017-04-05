package com.example.light;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.light.helper.ConnectionDetector;
import com.example.light.helper.DatabaseHelper;

public class LoginActivity extends Activity {
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
  	private ImageButton login;
  	private EditText user = null;
    private EditText password = null;
    private int status=0;
    private final int Suc=1;
    private final int Deny=2;
    private final int Fail=3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		login=(ImageButton) findViewById(R.id.imageButton1);
		user=(EditText) findViewById(R.id.login_user);
		password=(EditText) findViewById(R.id.login_pass);
		login.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Log.d("mark","login begin");
				connecting_Login();
				Log.d("mark","login end");
				
			}
		});
		
		
		DatabaseHelper dbHelper = new DatabaseHelper(LoginActivity.this,"ip_db");
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
			user.setText(id);
			Log.d("mark",id);
			password.setText(pass);
			Log.d("mark",pass);
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
			Log.d("mark","creat table");
		}
		db.close();
		dbHelper.close();
		
/////////////////////////////////////////////////////////////////////////////////
		soap_action = NAMESPACE + METHOD_NAME1;
		cd = new ConnectionDetector(getApplicationContext());
		if (cd.isConnectingToInternet() == false){
			Toast.makeText(LoginActivity.this, "请打开网络连接 :(", Toast.LENGTH_LONG).show();
			return;
		}
	}
	final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Log.d("mark","msg rec");
			switch(msg.what){
			case Suc:
				Log.d("mark","SUC");
				Intent intent1 = new Intent(LoginActivity.this,PageActivity.class);
				startActivity(intent1);	
				break;
			case Deny:
				Log.d("mark","DENY");
				Toast.makeText(LoginActivity.this, "用户名或密码错误 :(", Toast.LENGTH_LONG).show();
				break;
			case Fail:
				Log.d("mark","Fail");
				Toast.makeText(LoginActivity.this, "服务器不存在或连接超时 :）", Toast.LENGTH_LONG).show();
				break;
			}
			super.handleMessage(msg);
			
			
		}
	};
	private void connecting_Login(){
		Thread thread=new Thread(){
		public void run(){
		//////////////////////////////////////////////////////////////////
			DatabaseHelper dbHelper = new DatabaseHelper(LoginActivity.this,"ip_db");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Log.d("mark","query table");
			Cursor cursor = db.query("light_service", null, null, null, null, null, null);
			if (cursor.moveToNext()){
				namespace = cursor.getString(cursor.getColumnIndex("namespace"));
				url = cursor.getString(cursor.getColumnIndex("url"));
				id = user.getText().toString();
				Log.d("mark",id);
				pass = password.getText().toString();
				Log.d("mark",pass);
				method_name = cursor.getString(cursor.getColumnIndex("action1"));
				
				Log.d("mark","read table");
				
				db.delete("light_service", null, null);
				ContentValues values = new ContentValues();
				values.put("namespace", namespace);
				values.put("url", url);
				values.put("action1", method_name);
				values.put("id", id);
				values.put("password", pass);
				db.insert("light_service", null, values);
				Log.d("mark","save table");
			}
			else Log.d("mark","query failed");
			/////////////////////////////
			/*db.delete("light_service", null, null);
			ContentValues values = new ContentValues();
			values.put("namespace", namespace);
			values.put("url", url);
			values.put("action1", method_name);
			values.put("id", id);
			values.put("password", pass);
			db.insert("light_service", null, values);*/
			
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
		soap_action = namespace + "Login";
		SoapObject rpc = new SoapObject(namespace, "Login");
		rpc.addProperty("UserName", id);
		rpc.addProperty("UserPassword", pass);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = rpc;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(rpc);
		
		try{
		ht.call(soap_action, envelope);
		detail =(Object) envelope.getResponse();
		String result = detail.toString();
		System.out.println(result);
		if (result.startsWith("User logged in ")){
			Log.d("mark","access suc");
			status=Suc;
			//Intent intent1 = new Intent(LoginActivity.this,MainActivity.class);
			//startActivity(intent1);	
			SendMSG();
			
		}
		else if (result.startsWith("Failed")){
			status=Deny;
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
		//status=Fail;
		status=Fail;
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
	
	protected void onDestroy() {  
        super.onDestroy();  
          
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
