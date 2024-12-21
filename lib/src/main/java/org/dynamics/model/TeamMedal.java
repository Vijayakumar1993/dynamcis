package org.dynamics.model;

import java.util.Vector;

public class TeamMedal {
    private String teamName;
    private int gold;
    private int silver;
    private int bronze1;
    private int bronze2;

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = this.gold + gold;
    }

    public int getSilver() {
        return silver;
    }

    public void setSilver(int silver) {
        this.silver = this.silver + silver;
    }

    public int getBronze1() {
        return bronze1;
    }

    public void setBronze1(int bronze1) {
        this.bronze1 = this.bronze1 + bronze1;
    }

    public int getBronze2() {
        return bronze2;
    }

    public void setBronze2(int bronze2) {
        this.bronze2 = this.bronze2 + bronze2;
    }

    public static Vector<String> keys(){
        Vector<String> key = new Vector<>();
        key.add("Team Name");
        key.add("Gold");
        key.add("Silver");
        key.add("Bronze1");
        key.add("Bronze2");
        return key;
    }

    public Vector<Object> toVector(){
        Vector<Object> row = new Vector<>();
        row.add(this.teamName);
        row.add(this.gold);
        row.add(this.silver);
        row.add(this.bronze1);
        row.add(this.bronze2);
        return row;
    }
}
