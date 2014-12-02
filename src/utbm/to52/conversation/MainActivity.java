package utbm.to52.conversation;

import android.app.Activity;


import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity  {

	Database 		db;
	SQLiteDatabase 		dataBase;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		db = new Database(this);
	    db.open();
	    
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
    	menu.add(0,100,0,"Ajouter 3 questions à la bdd");
    	menu.add(0,200,0,"Nb de questions dans la bdd");
    	menu.add(0,300,0,"Nb de réponses dans la bdd");
    	menu.add(0,400,0,"Vider la bdd");
    	return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){

    	case 100: 
			db.viderBDD();
    		db.addQuestionIntoBDD("Comment allez vous aujourd'hui ?", "Comment tallez-vous aujourd'hui ?");
    		db.addQuestionIntoBDD("Qu'avez-vous pris pour le petit déjeuner ?", "Kavez-vous pris pour le petit déjeuner ?");
    		db.addQuestionIntoBDD("Avez vous pris vos médicaments ce matin ?", "Avez vous pris vos médicaments ce matin ?");
    		Toast.makeText(this, "3 questions ajoutées", Toast.LENGTH_LONG).show();
			break;
    	case 200: 
			Toast.makeText(this, "Il y a "+db.countValue_t_questions()+" questions", Toast.LENGTH_LONG).show();
			break;
    	case 300: 
			Toast.makeText(this, "Il y a "+db.countValue_t_reponses()+" réponses", Toast.LENGTH_LONG).show();
			break;
    	case 400: 
			db.viderBDD();
			Toast.makeText(this, "Bdd vidée", Toast.LENGTH_LONG).show();
			Toast.makeText(this, "Il y a "+db.countValue_t_questions()+" questions", Toast.LENGTH_LONG).show();
			Toast.makeText(this, "Il y a "+db.countValue_t_reponses()+" réponses", Toast.LENGTH_LONG).show();
			break;
    	}	
    	return true;
    }
}
