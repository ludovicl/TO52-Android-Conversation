package utbm.to52.conversation;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class Database {

	DatabaseHelper			DBHelper;
	Context					context;
    
	//declare sqlite database db
	public static SQLiteDatabase	db;
	@SuppressWarnings("unused")
	private static final int DATABASE_VERSION = 1;
	
	
	//constructor with call to DatabaseHelper
	public Database(Context context){
		this.context = context;
		DBHelper = new DatabaseHelper(context);
	}

	//
	public class DatabaseHelper extends SQLiteOpenHelper{

		Context			context;
		
		public DatabaseHelper(Context context) {
			super(context, "bdd_RecoVocale2", null, 1);  //  <---Version number : 1
			this.context = context;
		}

		//query for creating tables questions & answers
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

		//special function for upgrade the database, this function truncate the tables
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Toast.makeText(context, "MAJ de la BDD : V."+oldVersion+" -> V."+newVersion, Toast.LENGTH_SHORT).show();
			db.execSQL("DROP TABLE IF EXISTS t_questions");
			db.execSQL("DROP TABLE IF EXISTS t_reponses");
			onCreate(db);
		}
	}

	public Database open(){
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		db.close();
	}

	//truncate tables
	public void deleteData_t_questions(){
		db.execSQL("DELETE FROM t_questions");
	}
	public void deleteData_t_reponses(){
		db.execSQL("DELETE FROM t_reponses");
	}

	public void viderBDD(){
		deleteData_t_questions();
		deleteData_t_reponses();
	}
	
	//count values from tables
	public long  countValue_t_questions(){
		long nbVal= DatabaseUtils.queryNumEntries(db,"t_questions");
		return nbVal;
	}
	public long  countValue_t_reponses(){
		long nbVal= DatabaseUtils.queryNumEntries(db,"t_reponses");
		return nbVal;
	}

	//add some value into respective tables
	public long addQuestionIntoBDD(String questionPourEcrire, String questionPourLire){
		ContentValues values = new ContentValues();
				values.put("questionPourEcrire", questionPourEcrire);
				values.put("questionPourLire", questionPourLire );
			return db.insert("t_questions", null, values);
	}
	public long addAnswerIntoBDD(Integer idQuestion, String dateCourante, Integer numeroSession, String phraseReponse){
		ContentValues values = new ContentValues();
				values.put("idQuestion", idQuestion);
				values.put("dateCourante", dateCourante);
				values.put("numeroSession", numeroSession);
				values.put("phraseReponse", phraseReponse);
			return db.insert("t_reponses", null, values);
	}
	
	//function usefull for read datas
	public Cursor readQuestions(){
		return db.query("t_questions", new String[]{ "idQuestion", "questionPourEcrire", "questionPourLire"}, null, null, null, null, null);
	}
	public Cursor readAnswers(){
		return db.query("t_reponses", new String[]{ "idReponse", "idQuestion", "dateCourante", "numeroSession", "phraseReponse"}, null, null, null, null, null);
	}

	//get some question giving specific informations 
	public Integer getIdQuestions(String question) {
		int id = 0;
		Cursor cursor = readQuestions();
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
	//get some answers giving specific informations 	
	public String getQuestion(int id) {
		String question = "";
		Cursor cursor = readQuestions();
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
	
	//function to get list of questions
	public ArrayList<String> getListQuestionSpeak() {
	       
		ArrayList<String> listQuestionSpeak = new ArrayList<String>();
		Cursor cursor = readQuestions();
		
		if (cursor.moveToFirst()) {
	       do {
	    	   listQuestionSpeak.add(cursor.getString(2));
	       } while (cursor.moveToNext());
		}
		return listQuestionSpeak;
	}
	
	//function to get list of answers
	public ArrayList<String> getListQuestionView() {
	       
		ArrayList<String> listQuestionView = new ArrayList<String>();
		Cursor cursor = readQuestions();
		
		if (cursor.moveToFirst()) {
	       do {
	    	   listQuestionView.add(cursor.getString(1));
	       } while (cursor.moveToNext());
		}
		return listQuestionView;
	}
}
