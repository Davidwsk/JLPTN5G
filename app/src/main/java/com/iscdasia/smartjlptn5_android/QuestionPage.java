package com.iscdasia.smartjlptn5_android;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.iscdasia.smartjlptn5_android.databinding.FragmentQuestionPageBinding;

import java.util.ArrayList;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link QuestionPage.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QuestionPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuestionPage extends Fragment
        implements RadioGroup.OnCheckedChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private OnFragmentUpdateUserQuestionStatistic mUpdateUQSListener;

    private Question currentQuestion;

    private ArrayList<QuestionAnswer> answerArrayList;

    public QuestionPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuestionPage.
     */
    // TODO: Rename and change types and number of parameters
    public static QuestionPage newInstance(String param1, String param2) {
        QuestionPage fragment = new QuestionPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        currentQuestion = DataAccess.QUESTION_ARRAY_LIST.get(CurrentApp.CURRENT_QUESTION_POSITION_ID);

        final FragmentQuestionPageBinding fragmentQuestionPageBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_question_page, container, false);
        fragmentQuestionPageBinding.setQuestion(currentQuestion);

        Button finishButton = (Button) fragmentQuestionPageBinding.getRoot().findViewById(R.id.btnFinish);
        ImageButton prevImageButton = fragmentQuestionPageBinding.getRoot().findViewById(R.id.imgBtnPrevious);
        ImageButton nextImageButton = fragmentQuestionPageBinding.getRoot().findViewById(R.id.imgBtnNext);
        TextView descriptionTextView = fragmentQuestionPageBinding.getRoot().findViewById(R.id.tvDescription);

        finishButton.setVisibility(View.INVISIBLE);
        prevImageButton.setVisibility(View.INVISIBLE);
        nextImageButton.setVisibility(View.INVISIBLE);
        descriptionTextView.setVisibility(View.INVISIBLE);

        if (CurrentApp.CURRENT_QUESTION_POSITION_ID == 0 && CurrentApp.NO_OF_QUESTION > 1) {
            nextImageButton.setVisibility(View.VISIBLE);

        } else if (CurrentApp.CURRENT_QUESTION_POSITION_ID == CurrentApp.NO_OF_QUESTION - 1) {
            prevImageButton.setVisibility(View.VISIBLE);
            finishButton.setVisibility(View.VISIBLE);
        } else {
            prevImageButton.setVisibility(View.VISIBLE);
            nextImageButton.setVisibility(View.VISIBLE);
        }

        prevImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (CurrentApp.CURRENT_QUESTION_POSITION_ID > 0) {
                    CurrentApp.CURRENT_QUESTION_POSITION_ID = CurrentApp.CURRENT_QUESTION_POSITION_ID - 1;
                    mListener.onFragmentInteraction("RefreshQuestionPage");
                }
            }
        });


        nextImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (CurrentApp.CURRENT_QUESTION_POSITION_ID < CurrentApp.NO_OF_QUESTION) {
                    CurrentApp.CURRENT_QUESTION_POSITION_ID = CurrentApp.CURRENT_QUESTION_POSITION_ID + 1;
                    mListener.onFragmentInteraction("RefreshQuestionPage");
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CurrentApp.IsFinished = true;

                DataAccess.USER_QUESTION_STATISTIC_RESULT_ARRAY_LIST.clear();
                for (Question question :
                        DataAccess.QUESTION_ARRAY_LIST) {
                    String selectedValue = "";
                    String resultString = CurrentApp.CROSS_MARK;

                    for (QuestionAnswer questionAnswer :
                         DataAccess.QUESTION_ANSWER_ARRAY_LIST) {
                        if( question.getId() == questionAnswer.getQuestionId()  ){
                            if(questionAnswer.getSelected() == true) {
                                selectedValue = questionAnswer.getAnswerType();
                                if (questionAnswer.getAnswerType() == "0") {
                                    resultString = CurrentApp.CHECK_MARK;
                                }
                            }
                        }
                    }

                    String allUQStatistic = mUpdateUQSListener.onFragmentUpdateUserQuestionStatistic("MyUser",question.getId(),resultString,selectedValue);

                    DataAccess.USER_QUESTION_STATISTIC_RESULT_ARRAY_LIST.add( new UserQuestionStatisticResult(resultString,allUQStatistic));
                }

                mListener.onFragmentInteraction("ShowQuestionListPage");
            }
        });

        if (CurrentApp.IsFinished) {
            descriptionTextView.setVisibility(View.VISIBLE);
        }

        answerArrayList = new ArrayList<QuestionAnswer>();
        for (QuestionAnswer questionAnswer : DataAccess.QUESTION_ANSWER_ARRAY_LIST) {
            if (questionAnswer.getQuestionId() == currentQuestion.getId())
                answerArrayList.add(questionAnswer);
        }

        if (answerArrayList.size() == 0) {
            QuestionAnswer questionAnswer0 = new QuestionAnswer(currentQuestion.getId(), "0", currentQuestion.getCorrectAnswer(), false);
            QuestionAnswer questionAnswer1 = new QuestionAnswer(currentQuestion.getId(), "1", currentQuestion.getWrongAnswer1(), false);
            QuestionAnswer questionAnswer2 = new QuestionAnswer(currentQuestion.getId(), "2", currentQuestion.getWrongAnswer2(), false);
            QuestionAnswer questionAnswer3 = new QuestionAnswer(currentQuestion.getId(), "3", currentQuestion.getWrongAnswer3(), false);

            answerArrayList.add(questionAnswer0);
            answerArrayList.add(questionAnswer1);
            answerArrayList.add(questionAnswer2);
            answerArrayList.add(questionAnswer3);

            int listCount = answerArrayList.size();
            Random rnd = new Random();
            while (listCount > 1) {
                listCount--;
                int k = rnd.nextInt(listCount + 1);
                QuestionAnswer Tmp = answerArrayList.get(k);
                answerArrayList.set(k, answerArrayList.get(listCount));
                answerArrayList.set(listCount, Tmp);
            }

            DataAccess.QUESTION_ANSWER_ARRAY_LIST.addAll(answerArrayList);
        }

        RadioGroup rg = (RadioGroup) fragmentQuestionPageBinding.getRoot().findViewById(R.id.rdoGroupAnswer);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        //RadioGroup.LayoutParams paramsLayout = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.MATCH_PARENT);

        rg.setOnCheckedChangeListener(this);
        for (QuestionAnswer questionAnswer : answerArrayList) {
            RadioButton radioButton = new RadioButton(getContext());
            //radioButton.setId(Integer.parseInt(questionAnswer.getAnswerType()));
            radioButton.setId(Integer.parseInt(DataAccess.QUESTION_ANSWER_ARRAY_LIST.indexOf(questionAnswer) + "" + Integer.parseInt(questionAnswer.getAnswerType())));
            radioButton.setChecked(questionAnswer.getSelected());
            //radioButton.setId(DataAccess.QUESTION_ANSWER_ARRAY_LIST.indexOf(questionAnswer));
            if (CurrentApp.IsFinished) {
                radioButton.setText(questionAnswer.getAnswer() + "     " + (questionAnswer.getAnswerType() == "0" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK));
            } else {
                radioButton.setText(questionAnswer.getAnswer());
            }
            //radioButton.setId(1234);//set radiobutton id and store it somewhere
//            LinearLayout linearLayout = new LinearLayout(getContext());
//            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            //rg.addView(linearLayout,paramsLayout);
            rg.addView(radioButton, params);

//            TextView textView = new TextView(getContext());
//            textView.setText("X");
//            rg.addView(textView,params);
        }

        // Inflate the layout for this fragment
        //View view = inflater.inflate(R.layout.fragment_question_page, container, false);

        // NOTE : We are calling the onFragmentInteraction() declared in the MainActivity
// ie we are sending "Fragment 1" as title parameter when fragment1 is activated
        if (mListener != null) {
            mListener.onFragmentInteraction("Question");
        }

        return fragmentQuestionPageBinding.getRoot();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String title) {
        if (mListener != null) {
            mListener.onFragmentInteraction(title);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        if (context instanceof OnFragmentUpdateUserQuestionStatistic) {
            mUpdateUQSListener = (OnFragmentUpdateUserQuestionStatistic) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mUpdateUQSListener = null;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String checkValue = ("" + checkedId).substring(("" + checkedId).length()-1);

        for (QuestionAnswer questionAnswer :
                answerArrayList) {
            if (questionAnswer.getAnswerType().equals(checkValue)) {
                questionAnswer.setSelected(true);
            } else {
                questionAnswer.setSelected(false);
            }

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String title);
    }

    public interface OnFragmentUpdateUserQuestionStatistic {
        String onFragmentUpdateUserQuestionStatistic(String userId, String questionId, String result, String selectedValue);
    }


}


