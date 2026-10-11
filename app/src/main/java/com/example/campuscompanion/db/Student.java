package com.example.campuscompanion.db;

// Experiment 3: SQLite - model class for a student record

public class Student {
    private int id;
    private String name;
    private String roll;

    public Student(int id, String name, String roll) {
        this.id = id;
        this.name = name;
        this.roll = roll;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRoll() { return roll; }
}
