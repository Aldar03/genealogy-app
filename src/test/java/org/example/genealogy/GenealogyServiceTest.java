package org.example.genealogy;

import org.example.genealogy.model.Gender;
import org.example.genealogy.model.Person;
import org.example.genealogy.service.GenealogyService;
import org.example.genealogy.storage.Repository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


public class GenealogyServiceTest {


    @Test
    void addAndSearch() {
        Repository repo = new Repository();
        GenealogyService s = new GenealogyService(repo);
        Person p = s.addPerson("Басангова","Гиляна",null, LocalDate.of(1990,1,1), null, Gender.MALE);
        assertFalse(s.searchByName("Алдар").contains(p));
        assertTrue(s.searchByName("John").contains(p));
    }
}
