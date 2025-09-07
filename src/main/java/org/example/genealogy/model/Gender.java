package org.example.genealogy.model;

public enum Gender {
    MALE("мужской"),
    FEMALE("женский");

    private final String display;

    Gender(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}

