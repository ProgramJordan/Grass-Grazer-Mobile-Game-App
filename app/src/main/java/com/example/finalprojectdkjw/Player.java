package com.example.finalprojectdkjw;

public class Player {
    String _id;
    String name;
    int score;

    public Player() {}

    public Player(String _id, String name, int score) {
        this._id = _id;
        this.name = name;
        this.score = score;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return get_id() + "," + getName() + "," + getScore();
    }
}
