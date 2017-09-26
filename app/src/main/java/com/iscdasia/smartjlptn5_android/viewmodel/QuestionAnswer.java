package com.iscdasia.smartjlptn5_android.viewmodel;

/**
 * Created by iscd-dev01 on 22/9/2017.
 */

public class QuestionAnswer {
    private String QuestionId;
    private String AnswerType;
    private String Answer;
    private Boolean Selected;

    public QuestionAnswer(String questionId, String answerType, String answer, Boolean selected) {
        QuestionId = questionId;
        AnswerType = answerType;
        Answer = answer;
        Selected = selected;
    }

    public String getQuestionId() {
        return QuestionId;
    }

    public void setQuestionId(String questionId) {
        QuestionId = questionId;
    }

    public String getAnswerType() {
        return AnswerType;
    }

    public void setAnswerType(String answerType) {
        AnswerType = answerType;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public Boolean getSelected() {
        return Selected;
    }

    public void setSelected(Boolean selected) {
        Selected = selected;
    }
}
