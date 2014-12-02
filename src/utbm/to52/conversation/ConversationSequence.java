package utbm.to52.conversation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import utbm.to52.conversation.R;

class QuestionResponse {

	private String question;
	private String response;

	QuestionResponse(String q, String r) {
		this.question = q;
		this.response = r;
	}

	public String getQuestion() {
		return this.question;
	}

	public void setQuestion(String q) {
		this.question = q;
	}

	public String getResponse() {
		return this.response;
	}

	public void setResponse(String r) {
		this.response = r;
	}
}

public class ConversationSequence extends Activity {

	private TextToSpeech tts;
	private Handler modificationHandler;
	private Runnable waitModificationrunnable;
	private Button btn_change_response;
	private BaseDeDonnees db;
	private String response;
	private TextView textViewAnswer;
	private TextView textViewQuestion;

	private ArrayList<QuestionResponse> responseList = new ArrayList<QuestionResponse>();
	private ArrayList<String> qListView = new ArrayList<String>();
	private ArrayList<String> qListSpeak = new ArrayList<String>();
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private int numLastQuestion = 0;

	@Override
	protected void onCreate(Bundle SavedInstanceState) {

		super.onCreate(SavedInstanceState);
		setContentView(R.layout.conversation_sequence);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		db = new BaseDeDonnees(this);
		db.open();

		qListView = db.getListQuestionView();
		qListSpeak = db.getListQuestionSpeak();

		textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);
		textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);

		btn_change_response = (Button) findViewById(R.id.btnChangeResponse);
		btn_change_response.setVisibility(View.INVISIBLE);

		btn_change_response.setOnClickListener(new View.OnClickListener() {

			/*
			 * On click we remove the callback and we use questionAndGetResponse
			 * again => the same question is asked and the answer is deleted
			 */
			@Override
			public void onClick(View v) {
				modificationHandler.removeCallbacks(waitModificationrunnable);
				questionAndGetResponse();
				textViewAnswer.setText("");
			}
		});

		tts = new TextToSpeech(getBaseContext(),
				new TextToSpeech.OnInitListener() {

					@SuppressLint("NewApi")
					/*
					 * TextToSpeech initialization Can take few seconds to be
					 * initialized
					 */
					@Override
					public void onInit(int status) {
						if (status == TextToSpeech.SUCCESS) {

							/* HashMap used by setOnUtteranceProgressListener */
							HashMap<String, String> map = new HashMap<String, String>();

							map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
									"UniqueID");

							tts.setLanguage(Locale.FRANCE);

							tts.speak(
									"Bonjour, nous zallons vous poser une série de question",
									TextToSpeech.QUEUE_FLUSH, map);

							tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

								/*
								 * When speak is done wait for 2sec and start
								 * questionAndGetResponse();
								 */
								@Override
								public void onDone(String utteranceId) {

									if (utteranceId.equals("UniqueID")) {
										try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										questionAndGetResponse();
									}
								}

								@Override
								public void onError(String utteranceId) {
								}

								@Override
								public void onStart(String utteranceId) {
								}
							});
						}
					}
				});
	}

	@SuppressLint("NewApi")
	private void questionAndGetResponse() {

		/* Show the question in textView */
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				btn_change_response.setVisibility(View.VISIBLE);
				textViewQuestion.setText(qListView.get(0));
			}
		});

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID2");

		tts.speak(qListSpeak.get(0), TextToSpeech.QUEUE_FLUSH, map);

		/* When question is spoken, android listen with VoiceRecognition */
		tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onDone(String utteranceId) {

				if (utteranceId.equals("UniqueID2")) {
					startVoiceRecognitionActivity();
				}
			}

			@Override
			public void onError(String utteranceId) {
			}

			@Override
			public void onStart(String utteranceId) {
			}
		});

	}

	public void onPause() {
		if (tts != null) {
			tts.stop();
		}
		super.onPause();
	}

	/* Create the intent speech */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "à vous de répondre");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/* User has spoken, analyse answer */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {

			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			this.response = matches.get(0).toString();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);
					textViewAnswer.setText(response);
				}
			});

			/*
			 * Handler that wait 4sec before saving answer and continuing
			 * sequence
			 */
			modificationHandler = new Handler();
			modificationHandler.postDelayed(

			/* wait 4 sec 
			  user may want to change his answer */
			waitModificationrunnable = new Runnable() {
				@SuppressLint("SimpleDateFormat")
				public void run() {

					QuestionResponse qr = new QuestionResponse(
							qListView.get(0), response);

					responseList.add(qr);

					/* remove from the list after each question */
					qListView.remove(0);
					qListSpeak.remove(0);

					if (!qListView.isEmpty() && response != null) {
						textViewAnswer.setText("");
						questionAndGetResponse();
					} else { // we are done, every question has an answer
						for (QuestionResponse t_qr : responseList) {

							int idQuestion = db
									.getIdQuestionsDansTableQuestion(t_qr
											.getQuestion());

							Date d = new Date();
							SimpleDateFormat f = new SimpleDateFormat(
									"yyyyMMdd'T'HHmmss");
							String dateCourante = f.format(d);

							// add question in database
							db.ajouterReponseDansBDD(idQuestion, dateCourante,
									numLastQuestion, t_qr.getResponse());
						}

						HashMap<String, String> map = new HashMap<String, String>();
						map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
								"UniqueID3");

						tts.speak("Merci pour vos réponses et à bientôt",
								TextToSpeech.QUEUE_FLUSH, map);

						/*----Progress listener of "a bientot" for terminate activity at the end----*/
						tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
							@Override
							public void onDone(String utteranceId) {

								if (utteranceId.equals("UniqueID3")) {
									tts.shutdown();// close tts

									/* back do main activity */
									Intent intent = new Intent(
											ConversationSequence.this,
											MainActivity.class);
									startActivity(intent);
								}
							}

							@Override
							public void onError(String utteranceId) {
							}

							@Override
							public void onStart(String utteranceId) {
							}
						});
						/*------------------------------------------------------------------------*/

					}
				}
			}, 4000);// 4sec => waiting for possible correction
		}

		super.onActivityResult(requestCode, resultCode, data);

	}

}
