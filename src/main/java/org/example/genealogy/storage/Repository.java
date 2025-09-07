package org.example.genealogy.storage;

import org.example.genealogy.model.Person;

import java.util.*;


public class Repository {
    private final Map<UUID, Person> persons = new HashMap<>();

    public Collection<Person> findAll() {
        return persons.values();
    }

    public Optional<Person> findById(UUID id) {
        return Optional.ofNullable(persons.get(id));
    }

    public void save(Person p) {
        persons.put(p.getId(), p);
    }

    public void saveAll(Collection<Person> list) {
        for (Person p : list) save(p);
    }

    public void delete(UUID id) {
        persons.remove(id);
    }

    public void clear() {
        persons.clear();
    }
}
