package utbm.to52.conversation;

import android.app.Activity;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;

import android.view.View.OnClickListener;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btnStartConv = (Button) findViewById(R.id.btnStartConv);
		btnStartConv.setOnClickListener(new View.OnClickListener() {
			
			@Override
            public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,ConversationSequence.class);
				startActivity(intent);
				finish();
			}
            	
        });

		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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



//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.btnStartConv:
//			Intent intent = new Intent(MainActivity.this,ConversationSequence.class);
//			startActivity(intent);
//			break;
//		}
//	}




}
