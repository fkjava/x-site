package org.fkjava.identity.vo;

import org.fkjava.identity.domain.User;

public class AutoCompleteItem {

    private User user;
    private String value;

    public AutoCompleteItem(User user) {
        super();
        this.user = user;
        this.value = user.getName();
    }

    public User getUser() {
        return user;
    }

    public String getValue() {
        return value;
    }
}
