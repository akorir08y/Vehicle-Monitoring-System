package com.example.bus.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String full_name;
    private String number_plate;
    private String phone_number;
    private String email;
    private String username;
    private String avatar;
    private String user_id;

    public User(){

    }
    public User(String username, String email, String number_plate, String full_name, String phone_number) {
        this.full_name = full_name;
        this.number_plate = number_plate;
        this.phone_number = phone_number;
        this.email = email;
        this.username = username;
    }


    public User(String full_name, String number_plate, String phone_number, String email, String username, String avatar,String user_id) {
        this.full_name = full_name;
        this.number_plate = number_plate;
        this.phone_number = phone_number;
        this.email = email;
        this.username = username;
        this.user_id = user_id;
        this.avatar = avatar;
            }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNumber_plate() {
        return number_plate;
    }

    public void setNumber_plate(String number_plate) {
        this.number_plate = number_plate;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    protected User(Parcel in) {
        full_name = in.readString();
        number_plate = in.readString();
        phone_number = in.readString();
        user_id = in.readString();
        email = in.readString();
        username = in.readString();
        avatar = in.readString();
    }



    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(full_name);
        dest.writeString(number_plate);
        dest.writeString(phone_number);
        dest.writeString(user_id);
        dest.writeString(email);
        dest.writeString(username);
        dest.writeString(avatar);
    }
}