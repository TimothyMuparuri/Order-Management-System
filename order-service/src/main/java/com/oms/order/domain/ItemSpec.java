package com.oms.order.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ItemSpec {

    public enum SpecType { SEARCH_TERM, IMAGE }

    @Enumerated(EnumType.STRING)
    private SpecType type;

    private String searchTerm;
    private String objectKey;

    protected ItemSpec() {}

    public static ItemSpec searchTerm(String term) {
        ItemSpec s = new ItemSpec();
        s.type = SpecType.SEARCH_TERM;
        s.searchTerm = term;
        return s;
    }

    public static ItemSpec image(String objectKey) {
        ItemSpec s = new ItemSpec();
        s.type = SpecType.IMAGE;
        s.objectKey = objectKey;
        return s;
    }

    public SpecType getType() { return type; }
    public String getSearchTerm() { return searchTerm; }
    public String getObjectKey() { return objectKey; }
}
