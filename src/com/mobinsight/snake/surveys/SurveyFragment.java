package com.mobinsight.snake.surveys;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.mobinsight.client.Answer;
import com.mobinsight.client.AnswerRange;
import com.mobinsight.client.Mobinsight;
import com.mobinsight.client.Question;
import com.mobinsight.snake.R;

public class SurveyFragment extends Fragment {

    private static final String TAG = SurveyFragment.class.getName();

    private Mobinsight mobinsight;

    private TextView mSurveyNameText;
    private TextView mQuestionText;
    private ImageView mQuestionImage;
    private Button mAnswerButton;
    private View mProgressView;
    private EditText mAnswerText;
    private ListView mAnswerChoice;
    private SeekBar mAnswerRange;
    private TextView mSeekBarPosition;
    private TextView mSeekBarMin;
    private TextView mSeekBarMax;
    private Question mQuestion;
    private int mAnswerRangeMin;

    private Mobinsight.AuthenticateUserListener mAuthenticateUserListener =
            new Mobinsight.AuthenticateUserListener() {
        @Override
        public void onSuccess() {
            onAuthenticated();
        }
        @Override
        public void onError(int error) {
            showError("Authentication error " + error);
        }
    };

    private Mobinsight.GetNextQuestionListener mGetNextQuestionListener =
            new Mobinsight.GetNextQuestionListener() {
        @Override
        public void onSuccess(Question question) {
            onNextQuestion(question);
        }
        @Override
        public void onError(int error) {
            showError("getNextQuestion error " + error);
        }
    };

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.surveyfragment, container, false);
        view.setBackgroundResource(R.drawable.wood_dim);

        Mobinsight.initialize(getActivity(), getActivity().getPackageName());
        mSurveyNameText = (TextView) view.findViewById(R.id.survey_name);
        mQuestionText = (TextView) view.findViewById(R.id.question);
        mQuestionImage = (ImageView) view.findViewById(R.id.image);
        mAnswerButton = (Button) view.findViewById(R.id.answer);
        mProgressView = view.findViewById(R.id.progress);
        mAnswerText = (EditText) view.findViewById(R.id.answerText);
        mAnswerChoice = (ListView) view.findViewById(R.id.answerChoice);
        mAnswerRange = (SeekBar) view.findViewById(R.id.answerRange);
        mSeekBarPosition = (TextView) view.findViewById(R.id.seekbarPosition);
        mSeekBarMin = (TextView) view.findViewById(R.id.seekbarMin);
        mSeekBarMax = (TextView) view.findViewById(R.id.seekbarMax);         
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnswerButton.setEnabled(false);
                mProgressView.setVisibility(View.VISIBLE);
                Answer answer = null;
                if (mQuestion != null) {
	                if (mQuestion.getAnswerType() == Question.ANSWER_TYPE_TEXT) {
	                    answer = new Answer(mAnswerText.getText().toString());
	                } else if (mQuestion.getAnswerType() == Question.ANSWER_TYPE_CHOICE) {
	                    answer = new Answer(mAnswerChoice.getCheckedItemPosition());
	                } else if (mQuestion.getAnswerType() == Question.ANSWER_TYPE_RANGE) {
	                    answer = new Answer((float) mAnswerRange.getProgress());
	                }
                }
                mobinsight.answerLastQuestion(answer);
                mobinsight.getNextQuestion(mGetNextQuestionListener);
            }
        });

        mAnswerRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {        	

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				int position = mAnswerRangeMin + progress;
				mSeekBarPosition.setText(String.valueOf(position));
				mSeekBarPosition.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});
        
        mobinsight = Mobinsight.getInstance();
        if (mobinsight != null) {
        	mobinsight.authenticateUser(null, mAuthenticateUserListener);
        }
        
        return view;
    }

    private void onAuthenticated() {
        mobinsight.getNextQuestion(mGetNextQuestionListener);
    }

    private void onNextQuestion(com.mobinsight.client.Question question) {
        mAnswerButton.setEnabled(true);
        mProgressView.setVisibility(View.GONE);
        mAnswerText.setVisibility(View.GONE);
        mAnswerChoice.setVisibility(View.GONE);
        mAnswerRange.setVisibility(View.GONE);
        mSeekBarMin.setVisibility(View.GONE);
        mSeekBarMax.setVisibility(View.GONE);
        mSeekBarPosition.setVisibility(View.GONE);
        if (question != null) {
            mQuestion = question;
            mSurveyNameText.setText(question.getSurveyName() +
                    "\nQUESTION " + (question.getIndexInSurvey() + 1) + " OF UP TO " +
                    question.getSurveyLength());
            mSurveyNameText.setTextColor(Color.WHITE);            
            mQuestionText.setText(question.getText());
            mQuestionText.setTextColor(Color.WHITE);            
            if (question.getLocalImage() != null) {
                mQuestionImage.setVisibility(View.VISIBLE);
                mQuestionImage.setImageBitmap(BitmapFactory.decodeFile(question.getLocalImage()));
            } else {
                mQuestionImage.setVisibility(View.INVISIBLE);
            }
            if (question.getAnswerType() == Question.ANSWER_TYPE_TEXT) {
                mAnswerText.setVisibility(View.VISIBLE);
                mAnswerText.setText("");
            } else if (question.getAnswerType() == Question.ANSWER_TYPE_CHOICE) {
            	int mNumChoices = question.getAnswerChoices().size();
            	mAnswerChoice.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mNumChoices*66, getResources().getDisplayMetrics());            	
                mAnswerChoice.setVisibility(View.VISIBLE);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_multiple_choice,
                        question.getAnswerChoices());
                mAnswerChoice.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                mAnswerChoice.setAdapter(adapter);
                //mAnswerChoice.setItemChecked(0, true);                
            } else if (question.getAnswerType() == Question.ANSWER_TYPE_RANGE) {
                AnswerRange range = question.getAnswerRange();
                mAnswerRange.setMax((int) (range.mHigh - range.mLow));
                mAnswerRangeMin = (int) range.mLow;
                mAnswerRange.setVisibility(View.VISIBLE);
                mAnswerRange.setProgress(0);
                mSeekBarMin.setText(String.valueOf(range.mLow));
                mSeekBarMax.setText(String.valueOf(range.mHigh));
                mSeekBarMin.setVisibility(View.VISIBLE);
                mSeekBarMax.setVisibility(View.VISIBLE);
            }
        } else {
            mQuestionText.setText("Thanks for answering the questions. The level is now unlocked for you to enjoy!");
            mAnswerButton.setVisibility(View.INVISIBLE);
            mQuestionImage.setVisibility(View.INVISIBLE);
        }
    }

    private void showError(String message) {
        Log.e(TAG, message);
    }

}
