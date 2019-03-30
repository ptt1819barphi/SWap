package de.softlang.swap.dataclass;

import java.util.Map;

public class Noun {

    private String page_title;
    private Map<String, Integer> nouns;

    private Noun() {
        this(null, null);
    }

    public Noun(String page_title, Map<String, Integer> nouns) {
        this.page_title = page_title;
        this.nouns = nouns;
    }

    public String getPage_title() {
        return page_title;
    }

    public void setPage_title(String page_title) {
        this.page_title = page_title;
    }

    public Map<String, Integer> getNouns() {
        return nouns;
    }

    public void setNouns(Map<String, Integer> nouns) {
        this.nouns = nouns;
    }
}
