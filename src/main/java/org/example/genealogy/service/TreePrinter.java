package org.example.genealogy.service;

import org.example.genealogy.model.Person;
import java.util.List;


public class TreePrinter {

    public static void printLevels(List<List<Person>> levels, String title) {
        System.out.println("\n=== " + title + " ===");
        for (int i = 0; i < levels.size(); i++) {
            System.out.println("Уровень " + i + ":");
            for (Person p : levels.get(i)) {
                System.out.println("  - " + p);
            }
        }
    }
}
