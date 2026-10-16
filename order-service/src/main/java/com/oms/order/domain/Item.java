package com.oms.order.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "item")
public class Item {

    @Id
    private String itemRef;

    @Embedded
    private ItemSpec spec;

    protected Item() {}

    public Item(String itemRef, ItemSpec spec) {
        this.itemRef = itemRef;
        this.spec = spec;
    }

    public String getItemRef() { return itemRef; }
    public ItemSpec getSpec() { return spec; }
}
