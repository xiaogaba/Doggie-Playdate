package com.example.xin.pre_project.Model;
//driverApp & riderApp
public class Result {
    public String Message_id;
    public Result(){

    }
    public Result(String message_id) {
        Message_id = message_id;
    }

    public String getMessage_id() {
        return Message_id;
    }

    public void setMessage_id(String message_id) {
        Message_id = message_id;
    }
}
