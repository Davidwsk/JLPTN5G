package com.iscdasia.smartjlptn5_android.model;

import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;

/**
 * Created by iscd-dev01 on 26/9/2017.
 */

public class UserInformation {
    private String Id;
    private String UserName;
    private String Password;
    private String NoOfQuestion = "10";
    private DateTimeOffset lastUpdate;
    private Boolean IsPurchased_1;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getNoOfQuestion() {
        return NoOfQuestion;
    }

    public void setNoOfQuestion(String noOfQuestion) {
        NoOfQuestion = noOfQuestion;
    }

    public DateTimeOffset getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(DateTimeOffset lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Boolean getPurchased_1() {
        return IsPurchased_1;
    }

    public void setPurchased_1(Boolean purchased_1) {
        IsPurchased_1 = purchased_1;
    }
}
