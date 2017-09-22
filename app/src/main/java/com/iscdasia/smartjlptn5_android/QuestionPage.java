package com.iscdasia.smartjlptn5_android;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import static android.R.id.toggle;
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

        Question currentQuestion =  DataAccess.QUESTION_ARRAY_LIST.get(CurrentApp.CURRENT_QUESTION_POSITION_ID);

        FragmentQuestionPageBinding fragmentQuestionPageBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_question_page,container,false);
        fragmentQuestionPageBinding.setQuestion(currentQuestion);

        ArrayList<QuestionAnswer> answerArrayList = new ArrayList<QuestionAnswer>();
        for (QuestionAnswer questionAnswer:DataAccess.QUESTION_ANSWER_ARRAY_LIST)
        {
            if(questionAnswer.getQuestionId() == currentQuestion.getId())
                answerArrayList.add(questionAnswer);
        }

        if(answerArrayList.size() == 0)
        {
            QuestionAnswer questionAnswer0 = new QuestionAnswer(currentQuestion.getId(),"0",currentQuestion.getCorrectAnswer(),false);
            QuestionAnswer questionAnswer1 = new QuestionAnswer(currentQuestion.getId(),"1",currentQuestion.getWrongAnswer1(),false);
            QuestionAnswer questionAnswer2 = new QuestionAnswer(currentQuestion.getId(),"2",currentQuestion.getWrongAnswer2(),false);
            QuestionAnswer questionAnswer3 = new QuestionAnswer(currentQuestion.getId(),"3",currentQuestion.getWrongAnswer3(),false);

            answerArrayList.add(questionAnswer0);
            answerArrayList.add(questionAnswer1);
            answerArrayList.add(questionAnswer2);
            answerArrayList.add(questionAnswer3);

            int listCount = answerArrayList.size();
            Random rnd = new Random();
            while (listCount > 1)
            {
                listCount--;
                int k = rnd.nextInt(listCount + 1);
                QuestionAnswer Tmp = answerArrayList.get(k);
                answerArrayList.set(k,answerArrayList.get(listCount));
                answerArrayList.set(listCount,Tmp);
            }
            
            DataAccess.QUESTION_ANSWER_ARRAY_LIST.addAll(answerArrayList);
        }

        RadioGroup rg = (RadioGroup) fragmentQuestionPageBinding.getRoot().findViewById(R.id.rdoGroupAnswer);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        //RadioGroup.LayoutParams paramsLayout = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.MATCH_PARENT);

        rg.setOnCheckedChangeListener(this);
        for (QuestionAnswer questionAnswer: answerArrayList) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(questionAnswer.getAnswer() + "     " + (questionAnswer.getAnswerType() == "0"? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK) );
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.drawer_layout: {
                int i = 1;
                break;
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


}


