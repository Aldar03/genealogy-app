package org.example.genealogy.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class Person {
    private UUID id;
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private Gender gender;
    private Set<UUID> parentIds = new HashSet<>();
    private Set<UUID> childIds = new HashSet<>();
    private UUID spouseId; // 0..1

    public Person() {
    }

    public Person(String lastName, String firstName, String middleName,
                  LocalDate birthDate, LocalDate deathDate, Gender gender) {
        this.id = UUID.randomUUID();
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.gender = gender;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDate getDeathDate() { return deathDate; }
    public void setDeathDate(LocalDate deathDate) { this.deathDate = deathDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Set<UUID> getParentIds() { return parentIds; }
    public void setParentIds(Set<UUID> parentIds) { this.parentIds = parentIds; }

    public Set<UUID> getChildIds() { return childIds; }
    public void setChildIds(Set<UUID> childIds) { this.childIds = childIds; }

    public UUID getSpouseId() { return spouseId; }
    public void setSpouseId(UUID spouseId) { this.spouseId = spouseId; }


    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(lastName).append(" ").append(firstName);
        if (middleName != null) sb.append(" ").append(middleName);
        return sb.toString();
    }

    public String getShortId() {
        return id.toString().substring(0, 8);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lastName).append(" ").append(firstName);
        if (middleName != null) sb.append(" ").append(middleName);
        if (birthDate != null) sb.append(" рожд. ").append(birthDate);
        if (deathDate != null) sb.append(" ум. ").append(deathDate);
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
