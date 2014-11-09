package utbm.to52.conversation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.R.array;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
	private Button btn_speech;

	ArrayList<QuestionResponse> responseList = new ArrayList<QuestionResponse>();

	private ArrayList<String> qListView = new ArrayList<String>();
	private ArrayList<String> qListSpeak = new ArrayList<String>();

	Lock locker = new ReentrantLock();

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private String response;

	private TextView textViewAnswer;
	private TextView textViewQuestion;
	private final static Semaphore canSpeak = new Semaphore(1, true);
	final CountDownLatch latch = new CountDownLatch(1);

	@Override
	protected void onCreate(Bundle SavedInstanceState) {

		super.onCreate(SavedInstanceState);
		setContentView(R.layout.conversation_sequence);

		qListView.add("Comment allez vous aujourd'hui ?");
		qListSpeak.add("Comment tallez-vous aujourd'hui ?");

		qListView.add("Qu'avez-vous pris pour le petit déjeuner ?");
		qListSpeak.add("kavez-vous pris pour le petit déjeuner ?");

		textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);
		textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);

		btn_speech = (Button) findViewById(R.id.btnChangeResponse);
		
		btn_speech.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				modificationHandler.removeCallbacks(waitModificationrunnable);
				questionAndGetResponse();
				// TextView textViewAnswer = (TextView)
				// findViewById(R.id.textViewAnswer);
				textViewAnswer.setText("");
			}
		});

		tts = new TextToSpeech(getBaseContext(),
				new TextToSpeech.OnInitListener() {

					@SuppressLint("NewApi")
					@Override
					public void onInit(int status) {
						if (status == TextToSpeech.SUCCESS) {

							HashMap<String, String> map = new HashMap<String, String>();

							map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
									"UniqueID");

							tts.setLanguage(Locale.FRANCE);

							tts.speak(
									"Bonjour, nous zallons vous poser une série de question",
									TextToSpeech.QUEUE_FLUSH, map);

							tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

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
										if (qListView.size() == 0) {
											System.out.println(responseList);
										}
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
		// SystemClock.sleep(4000);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textViewQuestion.setText(qListView.get(0));
			}
		});
		//
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID2");

		tts.speak(qListSpeak.get(0), TextToSpeech.QUEUE_FLUSH, map);

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

	public boolean aquireLock() {
		try {
			canSpeak.acquire();
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	public boolean releaseLock() {
		canSpeak.release();
		return true;
	}

	private void startVoiceRecognitionActivity() {
		System.out.println("dans voice recognize");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "à vous de répondre");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

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

			/***
			 * Handler that wait 4sec before saving answer and continuing
			 * sequence
			 ****/
			modificationHandler = new Handler();
			modificationHandler.postDelayed(
					waitModificationrunnable = new Runnable() {
						public void run() {

							QuestionResponse qr = new QuestionResponse(
									qListView.get(0), response);
							responseList.add(qr);

							qListView.remove(0);
							qListSpeak.remove(0);

							if (qListView.size() != 0 && response != null) {
								textViewAnswer.setText("");
								questionAndGetResponse();
							} else {
								for (QuestionResponse t_qr : responseList) {
									System.out.println(t_qr.getQuestion());
									System.out.println(t_qr.getResponse());
								}
								tts.speak(
										"Merci pour vos réponses et à bientôt",
										TextToSpeech.QUEUE_FLUSH, null);
								// tts.shutdown();
							}

						}
					}, 4000);// 4sec => waiting for possible correction
			/**************************************************/

		}

		super.onActivityResult(requestCode, resultCode, data);

	}

}
