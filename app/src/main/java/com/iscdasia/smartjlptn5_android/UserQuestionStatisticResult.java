package com.iscdasia.smartjlptn5_android;

/**
 * Created by iscd-dev01 on 25/9/2017.
 */

public class UserQuestionStatisticResult {
    private String CurrentResult;
    private String AllResult;

    public UserQuestionStatisticResult(String currentResult, String allResult) {
        CurrentResult = currentResult;
        AllResult = allResult;
    }

    public String getCurrentResult() {
        return CurrentResult;
    }

    public void setCurrentResult(String currentResult) {
        CurrentResult = currentResult;
    }

    public String getAllResult() {
        return AllResult;
    }

    public void setAllResult(String allResult) {
        AllResult = allResult;
    }
}
