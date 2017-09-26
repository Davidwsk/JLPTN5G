package com.iscdasia.smartjlptn5_android.model;

import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;

/**
 * Created by iscd-dev01 on 25/9/2017.
 */

public class UserQuestionStatistic {
    private String Id;
    private String UserId;
    private String QuestionId;
    private DateTimeOffset lastUpdate;
    private String Round1Result;
    private String Round1Answer;
    private String Round2Result;
    private String Round2Answer;
    private String Round3Result;
    private String Round3Answer;
    private String Round4Result;
    private String Round4Answer;
    private String Round5Result;
    private String Round5Answer;
    private String Round6Result;
    private String Round6Answer;
    private String Round7Result;
    private String Round7Answer;
    private String Round8Result;
    private String Round8Answer;
    private String Round9Result;
    private String Round9Answer;
    private String Round10Result;
    private String Round10Answer;

    public UserQuestionStatistic(String userId, String questionId) {
        UserId = userId;
        QuestionId = questionId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getQuestionId() {
        return QuestionId;
    }

    public void setQuestionId(String questionId) {
        QuestionId = questionId;
    }

    public DateTimeOffset getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(DateTimeOffset lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getRound1Result() {
        return Round1Result;
    }

    public void setRound1Result(String round1Result) {
        Round1Result = round1Result;
    }

    public String getRound1Answer() {
        return Round1Answer;
    }

    public void setRound1Answer(String round1Answer) {
        Round1Answer = round1Answer;
    }

    public String getRound2Result() {
        return Round2Result;
    }

    public void setRound2Result(String round2Result) {
        Round2Result = round2Result;
    }

    public String getRound2Answer() {
        return Round2Answer;
    }

    public void setRound2Answer(String round2Answer) {
        Round2Answer = round2Answer;
    }

    public String getRound3Result() {
        return Round3Result;
    }

    public void setRound3Result(String round3Result) {
        Round3Result = round3Result;
    }

    public String getRound3Answer() {
        return Round3Answer;
    }

    public void setRound3Answer(String round3Answer) {
        Round3Answer = round3Answer;
    }

    public String getRound4Result() {
        return Round4Result;
    }

    public void setRound4Result(String round4Result) {
        Round4Result = round4Result;
    }

    public String getRound4Answer() {
        return Round4Answer;
    }

    public void setRound4Answer(String round4Answer) {
        Round4Answer = round4Answer;
    }

    public String getRound5Result() {
        return Round5Result;
    }

    public void setRound5Result(String round5Result) {
        Round5Result = round5Result;
    }

    public String getRound5Answer() {
        return Round5Answer;
    }

    public void setRound5Answer(String round5Answer) {
        Round5Answer = round5Answer;
    }

    public String getRound6Result() {
        return Round6Result;
    }

    public void setRound6Result(String round6Result) {
        Round6Result = round6Result;
    }

    public String getRound6Answer() {
        return Round6Answer;
    }

    public void setRound6Answer(String round6Answer) {
        Round6Answer = round6Answer;
    }

    public String getRound7Result() {
        return Round7Result;
    }

    public void setRound7Result(String round7Result) {
        Round7Result = round7Result;
    }

    public String getRound7Answer() {
        return Round7Answer;
    }

    public void setRound7Answer(String round7Answer) {
        Round7Answer = round7Answer;
    }

    public String getRound8Result() {
        return Round8Result;
    }

    public void setRound8Result(String round8Result) {
        Round8Result = round8Result;
    }

    public String getRound8Answer() {
        return Round8Answer;
    }

    public void setRound8Answer(String round8Answer) {
        Round8Answer = round8Answer;
    }

    public String getRound9Result() {
        return Round9Result;
    }

    public void setRound9Result(String round9Result) {
        Round9Result = round9Result;
    }

    public String getRound9Answer() {
        return Round9Answer;
    }

    public void setRound9Answer(String round9Answer) {
        Round9Answer = round9Answer;
    }

    public String getRound10Result() {
        return Round10Result;
    }

    public void setRound10Result(String round10Result) {
        Round10Result = round10Result;
    }

    public String getRound10Answer() {
        return Round10Answer;
    }

    public void setRound10Answer(String round10Answer) {
        Round10Answer = round10Answer;
    }
}
