package org.dynamics.model;

import java.io.Serializable;

public class PersonPair  implements Serializable {
    private Integer fromId;
    private Integer toId;

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }
}
