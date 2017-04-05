package com.example.light;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.light.helper.DatabaseHelper;

public class Configuration extends Activity implements OnClickListener{
	
	private static final String NAMESPACE = "http://cfins.au.tsinghua.edu.cn/";
  	private static final String URL = "http://166.111.73.6/aircontrol/MainServices.asmx";
  	private static final String METHOD_NAME1 = "Login";
  	private static final String ID = "2";
  	private static final String PASS = "cfins";
	
	private Button button_web_set = null;
    private Button button_web_reset = null;
    private EditText edit_web_namespace = null;
    private EditText edit_web_url = null;
    private EditText edit_web_action1 = null;
    private EditText edit_id = null;
    private EditText edit_pass = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);
		button_web_set = (Button)findViewById(R.id.button_set);
        button_web_set.setOnClickListener(this);
        button_web_reset = (Button)findViewById(R.id.button_reset);
        button_web_reset.setOnClickListener(this);
        edit_web_namespace = (EditText)findViewById(R.id.edit_namespace);
        edit_web_url = (EditText)findViewById(R.id.edit_url);
        edit_web_action1 = (EditText)findViewById(R.id.edit_action1);
        edit_id = (EditText)findViewById(R.id.edit_id);
        edit_pass = (EditText)findViewById(R.id.edit_pass);
        
        DatabaseHelper dbHelper = new DatabaseHelper(Configuration.this,"ip_db");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("light_service", null, null, null, null, null, null);
		if (cursor.moveToNext()){
			edit_web_namespace.setText( cursor.getString(cursor.getColumnIndex("namespace")) );
			edit_web_url.setText( cursor.getString(cursor.getColumnIndex("url")) );
			edit_web_action1.setText( cursor.getString(cursor.getColumnIndex("action1")) );
			edit_id.setText( cursor.getString(cursor.getColumnIndex("id")) );
			edit_pass.setText( cursor.getString(cursor.getColumnIndex("password")) );
		}
		db.close();
		dbHelper.close();
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	public void onClick(View v) {
		if(v.getId()==R.id.button_set){
			// TODO Auto-generated method stub
			 //创建一个DatabaseHelper对象
			DatabaseHelper dbHelper = new DatabaseHelper(Configuration.this,"ip_db");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete("light_service", null, null);
			ContentValues values = new ContentValues();
			values.put("namespace", edit_web_namespace.getText().toString());
			values.put("url", edit_web_url.getText().toString());
			values.put("action1", edit_web_action1.getText().toString());
			values.put("id", edit_id.getText().toString());
			values.put("password", edit_pass.getText().toString());
			db.insert("light_service", null, values);
			db.close();
			dbHelper.close();
			}
			else if(v.getId()==R.id.button_reset){
				DatabaseHelper dbHelper = new DatabaseHelper(Configuration.this,"ip_db");
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				db.delete("light_service", null, null);
				ContentValues values = new ContentValues();
				values.put("namespace", NAMESPACE);
				values.put("url", URL);
				values.put("action1", METHOD_NAME1);
				values.put("id", ID);
				values.put("password", PASS);
				db.insert("light_service", null, values);
				db.close();
				dbHelper.close();
				edit_web_namespace.setText(NAMESPACE);
				edit_web_url.setText(URL);
				edit_web_action1.setText(METHOD_NAME1);
				edit_id.setText(ID);
				edit_pass.setText(PASS);
			}
			
		}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.configuration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
