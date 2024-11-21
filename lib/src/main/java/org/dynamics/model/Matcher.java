package org.dynamics.model;

import java.io.Serializable;
import java.util.List;

public class Matcher  implements Serializable {
    private List<Match> matches;

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }
}
