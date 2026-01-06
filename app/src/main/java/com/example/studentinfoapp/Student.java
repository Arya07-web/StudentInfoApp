package com.example.studentinfoapp;

public class Student {
    public String rollNo;
    public String name;
    public int percentage;
    public int age;
    public String grade;
    public String department;
    public String year;

    public Student() {} // Default constructor required

    public Student(String rollNo, String name, int age,int percentage, String grade, String department, String year) {
        this.rollNo = rollNo;
        this.name = name;
        this.age=age;
        this.percentage = percentage;
        this.grade = grade;
        this.department = department;
        this.year = year;
    }
}
