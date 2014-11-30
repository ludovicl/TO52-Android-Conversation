package utbm.to52.conversation;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class BaseDeDonnees {

	DatabaseHelper			DBHelper;
	Context					context;
    
	public static SQLiteDatabase	db;
	@SuppressWarnings("unused")
	private static final int DATABASE_VERSION = 1;
	
	public BaseDeDonnees(Context context){
		this.context = context;
		DBHelper = new DatabaseHelper(context);
	}

	public class DatabaseHelper extends SQLiteOpenHelper{

		Context			context;
		
		public DatabaseHelper(Context context) {
			super(context, "bdd_RecoVocale1", null, 1);  //  <---Version number : 1
			this.context = context;
		}


		@Override
		public void onCreate(SQLiteDatabase db) {
				db.execSQL("create table t_questions (idQuestion integer primary key autoincrement, "
						+ "							questionPourEcrire text not null,"
						+ "							questionPourLire text not null "
						+ ");");
				
				db.execSQL("create table t_reponses (idReponse integer primary key autoincrement, "
						+ "							idQuestion integer not null, "
						+ "							dateCourante text not null, "
						+ "							numeroSession integer not null, "
						+ "							phraseReponse text not null "
						+ ");");
			}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Toast.makeText(context, "MAJ de la BDD : V."+oldVersion+" -> V."+newVersion, Toast.LENGTH_SHORT).show();
			db.execSQL("DROP TABLE IF EXISTS t_questions");
			db.execSQL("DROP TABLE IF EXISTS t_reponses");
			onCreate(db);
		}
	}

	public BaseDeDonnees open(){
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		db.close();
	}

	public void SupprimerDonnees_t_questions(){
		db.execSQL("DELETE FROM t_questions");
	}

	public void SupprimerDonnees_t_reponses(){
		db.execSQL("DELETE FROM t_reponses");
	}

	public void viderBDD(){
		SupprimerDonnees_t_questions();
		SupprimerDonnees_t_reponses();
	}
	
	public long  compterValeurs_t_questions(){
		long nbVal= DatabaseUtils.queryNumEntries(db,"t_questions");
		return nbVal;
	}
	
	public long  compterValeurs_t_reponses(){
		long nbVal= DatabaseUtils.queryNumEntries(db,"t_reponses");
		return nbVal;
	}

	public long ajouterQuestionDansBDD(String questionPourEcrire, String questionPourLire){
		ContentValues values = new ContentValues();
				values.put("questionPourEcrire", questionPourEcrire);
				values.put("questionPourLire", questionPourLire );
			return db.insert("t_questions", null, values);
	}

	public long ajouterReponseDansBDD(Integer idQuestion, String dateCourante, Integer numeroSession, String phraseReponse){
		ContentValues values = new ContentValues();
				values.put("idQuestion", idQuestion);
				values.put("dateCourante", dateCourante);
				values.put("numeroSession", numeroSession);
				values.put("phraseReponse", phraseReponse);
			return db.insert("t_reponses", null, values);
	}
	
	public Cursor lireDonneesQuestions(){
		return db.query("t_questions", new String[]{ "idQuestion", "questionPourEcrire", "questionPourLire"}, null, null, null, null, null);
	}

	public Cursor lireDonneesReponses(){
		return db.query("t_reponses", new String[]{ "idReponse", "idQuestion", "dateCourante", "numeroSession", "phraseReponse"}, null, null, null, null, null);
	}

	
	public Integer getIdQuestionsDansTableQuestion(String question) {
		int id = 0;
		Cursor cursor = lireDonneesQuestions();
		if (cursor.moveToFirst()) {
		   do {
			   if(question.equals(cursor.getString(1)))
			   {
				   id = Integer.parseInt(cursor.getString(0));
			   }
		   	} while (cursor.moveToNext());
		}
		return id;
	}
	
	public String getQuestionDansTableQuestion(int id) {
		String question = "";
		Cursor cursor = lireDonneesQuestions();
		if (cursor.moveToFirst()) {
		   do {
			   if(id == cursor.getInt(0))
			   {
				   question = cursor.getString(1);
			   }
		   	} while (cursor.moveToNext());
		}
		return question;
	}
	
	public ArrayList<String> getListQuestionSpeak() {
	       
		ArrayList<String> listQuestionSpeak = new ArrayList<String>();
		Cursor cursor = lireDonneesQuestions();
		
		if (cursor.moveToFirst()) {
	       do {
	    	   listQuestionSpeak.add(cursor.getString(2));
	       } while (cursor.moveToNext());
		}
		return listQuestionSpeak;
	}
	
	public ArrayList<String> getListQuestionView() {
	       
		ArrayList<String> listQuestionView = new ArrayList<String>();
		Cursor cursor = lireDonneesQuestions();
		
		if (cursor.moveToFirst()) {
	       do {
	    	   listQuestionView.add(cursor.getString(1));
	       } while (cursor.moveToNext());
		}
		return listQuestionView;
	}
}