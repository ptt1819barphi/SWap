package de.softlang.swap.dataclass;

import java.io.Serializable;
import java.util.Set;

public class NER implements Serializable {

    private String page_title;
    private Set<String> organizations;
    private Set<String> people;

    private NER() {
        this(null, null, null);
    }

    public NER(String page_title, Set<String> organizations, Set<String> people) {
        this.page_title = page_title;
        this.organizations = organizations;
        this.people = people;
    }

    public String getPage_title() {
        return page_title;
    }

    public void setPage_title(String page_title) {
        this.page_title = page_title;
    }

    public Set<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Set<String> organizations) {
        this.organizations = organizations;
    }

    public Set<String> getPeople() {
        return people;
    }

    public void setPeople(Set<String> people) {
        this.people = people;
    }
}
