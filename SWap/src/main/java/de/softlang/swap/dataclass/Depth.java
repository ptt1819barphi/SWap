package de.softlang.swap.dataclass;

import java.io.Serializable;

public class Depth implements Serializable {

    private String category;
    private int depth;

    public Depth() {
        this(null, 0);
    }

    public Depth(String category, int depth) {
        this.category = category;
        this.depth = depth;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
