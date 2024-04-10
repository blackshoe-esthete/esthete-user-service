package com.blackshoe.esthete.service;

public interface EmailSendService {
    boolean CheckAuthNum(String email,String authNum);
    void makeRandomNumber();
    String joinEmail(String email);
    void mailSend(String setFrom, String toMail, String title, String content);

}
