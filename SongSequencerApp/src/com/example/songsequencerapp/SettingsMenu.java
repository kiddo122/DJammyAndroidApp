package com.example.songsequencerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsMenu extends Activity {
	private Spinner dropdown;
	private Button btnSubmit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_menu);

		dropdown = (Spinner) findViewById(R.id.keys_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.planets_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		dropdown.setAdapter(adapter);

		// adapter.getDropDownView(int position, View convertView,
		// findViewById(R.id.keys_spinner);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	public void getDropdownValue(){
		dropdown = (Spinner) findViewById(R.id.keys_spinner);
		
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		 
		btnSubmit.setOnClickListener(new OnClickListener() {
	 
		  @Override
		  public void onClick(View view) {
	 
		    Toast.makeText(SettingsMenu.this,
			"OnClickListener : " + 
	                "\nSpinner 1 : "+ String.valueOf(dropdown.getSelectedItem()),
				Toast.LENGTH_SHORT).show();
		  }
	 
		});
	  }
	public void start_session(View view){
	
			Intent intent = new Intent(this, GameActivity.class);
			startActivity(intent);
		}
	
	}


