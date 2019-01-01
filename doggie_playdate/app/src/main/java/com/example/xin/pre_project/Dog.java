package com.example.xin.pre_project;

public class Dog {
    public int id;
    public String name;
    public String breed;
    public int gender, size;
    public int bdayMonth, bdayDay, bdayYear;
    public String profilePicPath;
    private static int idCounter = 0;

    public Dog(String name, String breed, int gender, int size, int year, int month, int day, String path) {
        this.id = idCounter++;
        this.name = name;
        this.breed = breed;
        this.gender = gender;
        this.size = size;
        this.bdayYear = year;
        this.bdayMonth = month;
        this.bdayDay = day;
        this.profilePicPath = path;
    }

    public void setProfilePic(String picPath) {
        this.profilePicPath = picPath;
    }
}
