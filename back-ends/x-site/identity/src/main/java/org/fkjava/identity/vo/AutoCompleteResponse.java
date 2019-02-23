package org.fkjava.identity.vo;

import org.fkjava.identity.domain.User;

import java.util.LinkedList;
import java.util.List;

public class AutoCompleteResponse {

    private List<AutoCompleteItem> suggestions;

    public AutoCompleteResponse(List<User> users) {
        super();
        this.suggestions = new LinkedList<>();
        users.forEach(u -> {
            AutoCompleteItem item = new AutoCompleteItem(u);
            this.suggestions.add(item);
        });
    }

    public List<AutoCompleteItem> getSuggestions() {
        return suggestions;
    }
}
