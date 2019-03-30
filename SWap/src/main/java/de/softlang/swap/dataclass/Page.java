package de.softlang.swap.dataclass;

import java.io.Serializable;

public class Page implements Serializable {

    private String page_title;
    private String text;

    public Page() {
        this(null, null);
    }

    public Page(String page_title, String text) {
        this.page_title = page_title;
        this.text = text;
    }

    public String getPage_title() {
        return page_title;
    }

    public void setPage_title(String page_title) {
        this.page_title = page_title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
