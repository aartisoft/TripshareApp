package com.example.tripshare.LoginRegister;

import com.google.gson.annotations.SerializedName;

/*
retrofit을 위한 model 클래스
서버에서 받은 json데이터를
키와 값으로 나눈다.
해당 키에 맞는 데이터를 가져온다.
즉 로그인 여부와 사용자 이름을 가져온다.

*/

public class User {
    //서버에서 받아온 json 데이터에의 키가 response였다.
    //자바에선는 변수명이 Response으로 바꾸겠다는 의미
    @SerializedName("response")
     String Response;

    @SerializedName("name")
    private String Name;

    @SerializedName("email")
    private String email;
    private String password;
    @SerializedName("image")
    private String image;
    private String method;
    private boolean checkedornot;

    public User(String name, String image) {
        Name = name;
        this.image = image;
    }

    public boolean isCheckedornot() {
        return checkedornot;
    }

    public void setCheckedornot(boolean checkedornot) {
        this.checkedornot = checkedornot;
    }

    public String getMethod() {
        return method;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getImage() {
        return image;
    }

    public String getResponse() {
        return Response;
    }

    public String getName() {
        return Name;
    }
}
