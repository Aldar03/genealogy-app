package org.example.genealogy.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.genealogy.model.Person;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class JsonStorage {
    private final Gson gson;

    public JsonStorage() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void save(String path, Collection<Person> persons) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            gson.toJson(persons, fw);
        }
    }

    public List<Person> load(String path) throws IOException {
        try (FileReader fr = new FileReader(path)) {
            Person[] arr = gson.fromJson(fr, Person[].class);
            List<Person> list = new ArrayList<>();
            if (arr != null) {
                for (Person p : arr) list.add(p);
            }
            return list;
        }
    }
}
