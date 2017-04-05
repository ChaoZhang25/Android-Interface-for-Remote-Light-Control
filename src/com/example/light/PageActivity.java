package com.example.light;

import java.util.ArrayList;
import java.util.List;

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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.light.helper.ConnectionDetector;
import com.example.light.helper.DatabaseHelper;
public class PageActivity extends Activity {
	private View view1, view2, view3;//需要滑动的页卡  
    private ViewPager viewPager;//viewpager  
    private PagerTitleStrip pagerTitleStrip;//viewpager的标题  
    private PagerTabStrip pagerTabStrip;//一个viewpager的指示器，效果就是一个横的粗的下划线  
    private List<View> viewList;//把需要滑动的页卡添加到这个list中  
    private List<String> titleList;//viewpager的标题 
    /////////////////////////////////////////////////////////////
    private SeekBar fan_bar;// 
    private int fan_speed=0;
    private SeekBar lighton_bar;// 
    //private int on_time=0;
    private SeekBar lightoff_bar;// 
    //private int off_time=0;
    //private Intent intent; 
    private Button button1;
 	private ImageView imageView1=null;
 	private Button button2;
 	private ImageView imageView2=null;
 	private Button button3;
 	private ImageView imageView3=null;
 	private ImageButton up;
 	private ImageButton down;
 	private ImageButton stop;
 	private ImageButton mur_windy;
 	private ImageButton mur_cold;
 	private ImageButton mur_hot;
 	private ImageButton mur_stuffy;
 	private ImageButton mur_dark;
 	private ImageButton mur_noisy;
 	private ImageButton mur_glare;
 	private ImageButton mur_damp;
 	private ImageButton mur_dry;
 	private TextView windspeed;
 	private TextView on_time;
 	private TextView off_time;
    ///////////////////////////////////////////////////////////////////////////////////////
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
 	private final int MSG_M_SUC=4;
 	private final int MSG_M_DENY=5;
 	private final int MSG_O_SUC=40;
 	private final int MSG_O_DENY=50;
 	private int status=3;
 	
 	private int curtain_action=0;
 	private String mur="";
 	private int c_status=0;
 	private int f_status=0;
 	private boolean log=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page);
		log=false;
		
		DatabaseHelper dbHelper = new DatabaseHelper(PageActivity.this,"ip_db");
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
			Toast.makeText(PageActivity.this, "请打开网络连接 :(", Toast.LENGTH_LONG).show();
			return;
		}
		
		initView();
	}
	private void initView() { 
        viewPager = (ViewPager) findViewById(R.id.viewpager); 
        //pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pagertitle); 
        pagerTabStrip=(PagerTabStrip) findViewById(R.id.pagertab); 
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.gold));  
        pagerTabStrip.setDrawFullUnderline(false); 
        pagerTabStrip.setBackgroundColor(getResources().getColor(R.color.azure)); 
        pagerTabStrip.setTextSpacing(50); 
        
        view1 = findViewById(R.layout.activity_main);  
        view2 = findViewById(R.layout.layout2);  
        view3 = findViewById(R.layout.layout3);  
  
        LayoutInflater lf = getLayoutInflater().from(this);  
        view1 = lf.inflate(R.layout.activity_main, null);  
        view2 = lf.inflate(R.layout.layout2, null);  
        view3 = lf.inflate(R.layout.layout3, null); 
        
        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中  
        viewList.add(view1);  
        viewList.add(view2);  
        viewList.add(view3);  
  
        titleList = new ArrayList<String>();// 每个页面的Title数据  
        titleList.add("开关");  
        titleList.add("抱怨");  
        titleList.add("灯光预约"); 
        
        PagerAdapter pagerAdapter = new PagerAdapter() {  
        	  
            @Override  
            public boolean isViewFromObject(View arg0, Object arg1) {  
  
                return arg0 == arg1;  
            }  
  
            @Override  
            public int getCount() {  
  
                return viewList.size();  
            }  
  
            @Override  
            public void destroyItem(ViewGroup container, int position,  
                    Object object) {  
                container.removeView(viewList.get(position));  
  
            }  
  
            @Override  
            public int getItemPosition(Object object) {  
  
                return super.getItemPosition(object);  
            }  
  
            @Override  
            public CharSequence getPageTitle(int position) {  
  
                return titleList.get(position);//直接用适配器来完成标题的显示，所以从上面可以看到，我们没有使用PagerTitleStrip。当然你可以使用。  
  
            }  
  
            @Override  
            public Object instantiateItem(ViewGroup container, int position) {  
                container.addView(viewList.get(position));  
                int p=position;
                if(p==0){
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
        		fan_bar=(SeekBar)findViewById(R.id.seekBar1);
        		fan_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
        			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {//seekbar监听操作  
        	            //当拖动时会调用该函数 arg0表示当前操作的控件，arg1为当前拖动的位置，arg2为表示是否是用户改变的进度  
        	            if(arg0.getId()==R.id.seekBar1)  
        	            {
        	            	windspeed=(TextView)findViewById(R.id.textView1);
        	            	switch(arg1){
        	            	case 0:
        	            		windspeed.setText("风速：0");
        	            		f_status=0;
        	            		break;
        	            	case 1:
        	            		windspeed.setText("风速：低");
        	            		f_status=1;
        	            		break;
        	            	case 2:
        	            		windspeed.setText("风速：中");
        	            		f_status=1;
        	            		break;
        	            	case 3:
        	            		windspeed.setText("风速：高");
        	            		f_status=1;
        	            		break;
        	            	
        	            	}
        	            	fan_speed=arg1;
        	            	Log.d("mark","connecting fan begin");
            				connecting_Fan();
            				Log.d("mark","connecting end"); 
        	            }         
        	        }  
        	  
        	        @Override  
        	        public void onStartTrackingTouch(SeekBar arg0) {  
        	            // TODO Auto-generated method stub  
        	              
        	        }  
        	  
        	        @Override  
        	        public void onStopTrackingTouch(SeekBar arg0) {  
        	            // TODO Auto-generated method stub  
        	              
        	        }  
        		});
        		
        		
        		imageView3=(ImageView) findViewById(R.id.imageView3);
        		/*button3=(Button)findViewById(R.id.button3);
        		button3.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting curtain begin");
        				connecting_Curtain();
        				Log.d("mark","connecting end");
        				
        			}
        		});*/
        		up=(ImageButton)findViewById(R.id.upButton);
        		up.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting curtain begin");
        				curtain_action=1;
        				connecting_Curtain();
        				Log.d("mark","connecting end");
        				
        			}
        		});
        		
        		down=(ImageButton)findViewById(R.id.downButton);
        		down.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting curtain begin");
        				curtain_action=2;
        				connecting_Curtain();
        				Log.d("mark","connecting end");
        				
        			}
        		});
        		
        		stop=(ImageButton)findViewById(R.id.stopButton);
        		stop.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting curtain begin");
        				curtain_action=3;
        				connecting_Curtain();
        				Log.d("mark","connecting end");
        				
        			}
        		});
                }
                if(p==1){
        		mur_windy=(ImageButton)findViewById(R.id.button_mur_windy);
        		mur_windy.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="windy";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_hot=(ImageButton)findViewById(R.id.button_mur_hot);
        		mur_hot.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="hot";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_cold=(ImageButton)findViewById(R.id.button_mur_cold);
        		mur_cold.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="cold";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_stuffy=(ImageButton)findViewById(R.id.button_mur_stuffy);
        		mur_stuffy.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="stuffy";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_noisy=(ImageButton)findViewById(R.id.button_mur_noisy);
        		mur_noisy.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="noisy";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_glare=(ImageButton)findViewById(R.id.button_mur_glare);
        		mur_glare.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="glare";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_dark=(ImageButton)findViewById(R.id.button_mur_dark);
        		mur_dark.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="dark";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_damp=(ImageButton)findViewById(R.id.button_mur_damp);
        		mur_damp.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="damp";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
        		
        		mur_dry=(ImageButton)findViewById(R.id.button_mur_dry);
        		mur_dry.setOnClickListener(new OnClickListener(){
        			public void onClick(View v){
        				Log.d("mark","connecting mur begin");
        				mur="dry";
        				connecting_Mur();
        				Log.d("mark","connecting end");
        			}
        		});
                }
                if(p==2){
                	lighton_bar=(SeekBar)findViewById(R.id.lightonBar);
            		lighton_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {//seekbar监听操作  
            	            //当拖动时会调用该函数 arg0表示当前操作的控件，arg1为当前拖动的位置，arg2为表示是否是用户改变的进度  
            	           
            	            	on_time=(TextView)findViewById(R.id.textView2);
            	            	on_time.setText("灯光将在"+arg1+"分钟后打开");           	                    
            	        }  
            	  
            	        @Override  
            	        public void onStartTrackingTouch(SeekBar arg0) {  
            	            // TODO Auto-generated method stub  
            	              
            	        }  
            	  
            	        @Override  
            	        public void onStopTrackingTouch(SeekBar arg0) {  
            	            // TODO Auto-generated method stub  
            	              
            	        }  
            		});
            		
            		lightoff_bar=(SeekBar)findViewById(R.id.lightoffBar);
            		lightoff_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {//seekbar监听操作  
            	            //当拖动时会调用该函数 arg0表示当前操作的控件，arg1为当前拖动的位置，arg2为表示是否是用户改变的进度  
            	           
            	            	off_time=(TextView)findViewById(R.id.textView3);
            	            	off_time.setText("灯光将在"+arg1+"分钟后关闭");           	                    
            	        }  
            	  
            	        @Override  
            	        public void onStartTrackingTouch(SeekBar arg0) {  
            	            // TODO Auto-generated method stub  
            	              
            	        }  
            	  
            	        @Override  
            	        public void onStopTrackingTouch(SeekBar arg0) {  
            	            // TODO Auto-generated method stub  
            	              
            	        }  
            		});
            		Button send = (Button)findViewById(R.id.button2);
            		send.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							
						}
            		});
                }
                return viewList.get(position);  
            }  
  
        };  
        viewPager.setAdapter(pagerAdapter);  
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
				Toast.makeText(PageActivity.this, "灯光操作成功 :）", Toast.LENGTH_LONG).show();
				break;
			case MSG_L_DENY:
				Log.d("mark","MSG_L_DENY");
				Toast.makeText(PageActivity.this, "灯光操作失败 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_F_SUC:
				Log.d("mark","MSG_F_SUC");
				if(f_status==1)
					imageView2.setImageResource(R.drawable.fan_on);
					else imageView2.setImageResource(R.drawable.fan_off);
				Toast.makeText(PageActivity.this, "风扇操作成功 :）", Toast.LENGTH_LONG).show();
				break;
			case MSG_F_DENY:
				Log.d("mark","MSG_F_DENY");
				if(f_status==1)
					imageView2.setImageResource(R.drawable.fan_on);
				else imageView2.setImageResource(R.drawable.fan_off);
				Toast.makeText(PageActivity.this, "风扇操作失败 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_C_SUC:
				Log.d("mark","MSG_C_SUC");
				if(curtain_action!=3)
					imageView3.setImageResource(R.drawable.curtain_on);
				else imageView3.setImageResource(R.drawable.curtain_off);
				Toast.makeText(PageActivity.this, "窗帘操作成功 :）", Toast.LENGTH_LONG).show();
				break;
			case MSG_C_DENY:
				Log.d("mark","MSG_C_DENY");
				if(curtain_action!=3)
					imageView3.setImageResource(R.drawable.curtain_on);
				else imageView3.setImageResource(R.drawable.curtain_off);
				Toast.makeText(PageActivity.this, "窗帘操作失败 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_TIMEOUT:
				Log.d("mark","MSG_TIMEOUT");
				Toast.makeText(PageActivity.this, "服务器不存在或连接超时 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_M_SUC:
				Log.d("mark","MSG_M_SUC");
				Toast.makeText(PageActivity.this, "抱怨成功！ :)", Toast.LENGTH_LONG).show();
				break;
			case MSG_M_DENY:
				Log.d("mark","MSG_M_DENY");
				Toast.makeText(PageActivity.this, "抱怨失败 :(", Toast.LENGTH_LONG).show();
				break;
			case MSG_O_SUC:
				Log.d("mark","MSG_O_SUC");
				Toast.makeText(PageActivity.this, "注销成功！ :)", Toast.LENGTH_LONG).show();
				break;
			case MSG_O_DENY:
				Log.d("mark","MSG_O_DENY");
				Toast.makeText(PageActivity.this, "注销失败 :(", Toast.LENGTH_LONG).show();
				break;
			case 0:
				Toast.makeText(PageActivity.this, "服务器出错啦！赶快调! @.@", Toast.LENGTH_LONG).show();
			}
			super.handleMessage(msg);
			
			
		}
	};

	private void connecting_Fan(){
		Thread thread=new Thread(){
		public void run(){
		//////////////////////////////////////////////////////////////////
			DatabaseHelper dbHelper = new DatabaseHelper(PageActivity.this,"ip_db");
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
		rpc.addProperty("UserName", id);
		rpc.addProperty("UserPassword", pass);
		/////////////////////////////////
		rpc.addProperty("cmd",fan_speed);
		/////////////////////////////////
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
			DatabaseHelper dbHelper = new DatabaseHelper(PageActivity.this,"ip_db");
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
		soap_action = namespace + "BlindsControl";
		SoapObject rpc = new SoapObject(namespace,"BlindsControl");
		rpc.addProperty("UserName", id);
		rpc.addProperty("UserPassword", pass);
		//////////////////////////////////
		rpc.addProperty("cmd",curtain_action);
		rpc.addProperty("seq",1);
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
			DatabaseHelper dbHelper = new DatabaseHelper(PageActivity.this,"ip_db");
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
	
	private void connecting_Mur(){
		Thread thread=new Thread(){
		public void run(){
		//////////////////////////////////////////////////////////////////
			DatabaseHelper dbHelper = new DatabaseHelper(PageActivity.this,"ip_db");
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
		soap_action = namespace + "Complain";
		SoapObject rpc = new SoapObject(namespace, "Complain");
		rpc.addProperty("UserName", id);
		rpc.addProperty("UserPassword", pass);
		rpc.addProperty("UserCompType", mur);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = rpc;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(rpc);
		
		try{
		ht.call(soap_action, envelope);
		detail =(Object) envelope.getResponse();
		String result = detail.toString();
		System.out.println(result);
		if (result.startsWith("User")){
			Log.d("mark","access suc");
			status=MSG_M_SUC;
			SendMSG();
			
		}
		else if (result.startsWith("Failed")){
			status=MSG_M_DENY;
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
	
	private void connecting_Logout(){
		Thread thread=new Thread(){
		public void run(){
		//////////////////////////////////////////////////////////////////
			DatabaseHelper dbHelper = new DatabaseHelper(PageActivity.this,"ip_db");
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
		soap_action = namespace + "Logout";
		SoapObject rpc = new SoapObject(namespace, "Logout");
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
		if (result.startsWith("User logged out")){
			Log.d("mark","access suc");
			status=MSG_O_SUC;
			SendMSG();
			
		}
		else if (result.startsWith("Failed")){
			status=MSG_O_DENY;
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
	protected void onDestroy() { 
		connecting_Logout();
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

	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.page, menu);
		return true;
	}

}*/
