package com.famistry.famistry_personnel.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RelationshipType {
    FATHER("father"), MOTHER("mother"), PARTNER("partner"), PARENT("parent");

    private final String value;

    RelationshipType(String value) { this.value = value; }

    @JsonValue
    public String value() { return value; }

    @JsonCreator
    public static RelationshipType from(String v) {
        if (v == null) return null;
        String s = v.trim().toLowerCase();
        for (RelationshipType t : values()) if (t.value.equals(s)) return t;
        throw new IllegalArgumentException("Invalid relationship type: " + v + ". Allowed: father,mother,partner,parent");
    }
}
