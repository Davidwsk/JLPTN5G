package com.iscdasia.smartjlptn5_android;

import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;

/**
 * Created by iscd-dev01 on 14/9/2017.
 */

public class Question {
    public Question() {
    }

    @com.google.gson.annotations.SerializedName("deleted")
    private Boolean mDeleted;
    public Boolean getDeleted() { return  mDeleted;}
    public final void setDeleted(Boolean deleted){ mDeleted = deleted;}
    public boolean isDeleted() {
        return mDeleted;
    }

    /// <summary>
    /// Id for item
    /// </summary>
    @com.google.gson.annotations.SerializedName("id")
    private String mId;
    public String getId() { return mId; }
    public final void setId(String id) { mId = id; }

    @com.google.gson.annotations.SerializedName("questionGroupId")
    private String mQuestionGroupId;

    public String getQuestionGroupId() {
        return mQuestionGroupId;
    }

    public final void setQuestionGroupId(String mQuestionGroupId) {
        this.mQuestionGroupId = mQuestionGroupId;
    }

    @com.google.gson.annotations.SerializedName("questionText")
    public String mQuestionText;

    public String getQuestionText() {
        return mQuestionText;
    }

    public final void setQuestionText(String mQuestionText) {
        this.mQuestionText = mQuestionText;
    }

    @com.google.gson.annotations.SerializedName("correctAnswer")
    private String mCorrectAnswer;

    public String getCorrectAnswer() {
        return mCorrectAnswer;
    }

    public final void setCorrectAnswer(String mCorrectAnswer) {
        this.mCorrectAnswer = mCorrectAnswer;
    }

    @com.google.gson.annotations.SerializedName("wrongAnswer1")
    private String mWrongAnswer1;

    public String getWrongAnswer1() {
        return mWrongAnswer1;
    }

    public final void setWrongAnswer1(String mWrongAnswer1) {
        this.mWrongAnswer1 = mWrongAnswer1;
    }

    @com.google.gson.annotations.SerializedName("wrongAnswer2")
    private String mWrongAnswer2;

    public String getWrongAnswer2() {
        return mWrongAnswer2;
    }

    public final void setWrongAnswer2(String mWrongAnswer2) {
        this.mWrongAnswer2 = mWrongAnswer2;
    }

    @com.google.gson.annotations.SerializedName("wrongAnswer3")
    private String mWrongAnswer3;

    public String getWrongAnswer3() {
        return mWrongAnswer3;
    }

    public final void setWrongAnswer3(String mWrongAnswer3) {
        this.mWrongAnswer3 = mWrongAnswer3;
    }

    @com.google.gson.annotations.SerializedName("description")
    private String mDescription;

    public String getDescription() {
        return mDescription;
    }

    public final void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Question && ((Question) o).mId == mId;
    }
}
