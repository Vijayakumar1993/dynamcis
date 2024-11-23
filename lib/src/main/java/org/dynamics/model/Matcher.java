package org.dynamics.model;

import java.io.Serializable;
import java.util.List;

public class Matcher  implements Serializable {
    private List<Match> matches;

    private Person winner;
    private Corner winnerCorder;

    public Corner getWinnerCorder() {
        return winnerCorder;
    }

    public void setWinnerCorder(Corner winnerCorder) {
        this.winnerCorder = winnerCorder;
    }

    public Person getWinner() {
        return winner;
    }

    public void setWinner(Person winner) {
        this.winner = winner;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }
}
