package org.example.genealogy.service;

import org.example.genealogy.model.Gender;
import org.example.genealogy.model.Person;
import org.example.genealogy.storage.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class GenealogyService {
    private final Repository repo;

    public GenealogyService(Repository repo) {
        this.repo = repo;
    }


    public Person addPerson(String last, String first, String middle,
                            LocalDate birth, LocalDate death, Gender gender) {
        Person p = new Person(last, first, middle, birth, death, gender);
        repo.save(p);
        return p;
    }

    public boolean editPerson(UUID id, String last, String first, String middle,
                              LocalDate birth, LocalDate death, Gender gender) {
        Optional<Person> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        Person p = opt.get();
        if (last != null) p.setLastName(last);
        if (first != null) p.setFirstName(first);
        if (middle != null) p.setMiddleName(middle);
        if (birth != null) p.setBirthDate(birth);
        if (death != null || (death == null)) p.setDeathDate(death);
        if (gender != null) p.setGender(gender);
        repo.save(p);
        return true;
    }

    public boolean deletePerson(UUID id) {
        Optional<Person> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        Person p = opt.get();
        for (UUID parentId : new HashSet<>(p.getParentIds())) {
            repo.findById(parentId).ifPresent(parent -> parent.getChildIds().remove(p.getId()));
        }
        for (UUID childId : new HashSet<>(p.getChildIds())) {
            repo.findById(childId).ifPresent(child -> child.getParentIds().remove(p.getId()));
        }
        if (p.getSpouseId() != null) {
            repo.findById(p.getSpouseId()).ifPresent(sp -> sp.setSpouseId(null));
        }

        repo.delete(id);
        return true;
    }


    public boolean linkParentChild(UUID parentId, UUID childId) {
        Optional<Person> pOpt = repo.findById(parentId);
        Optional<Person> cOpt = repo.findById(childId);
        if (pOpt.isEmpty() || cOpt.isEmpty()) return false;
        Person parent = pOpt.get();
        Person child = cOpt.get();
        parent.getChildIds().add(child.getId());
        child.getParentIds().add(parent.getId());
        return true;
    }

    public boolean unlinkParentChild(UUID parentId, UUID childId) {
        Optional<Person> pOpt = repo.findById(parentId);
        Optional<Person> cOpt = repo.findById(childId);
        if (pOpt.isEmpty() || cOpt.isEmpty()) return false;
        Person parent = pOpt.get();
        Person child = cOpt.get();
        parent.getChildIds().remove(child.getId());
        child.getParentIds().remove(parent.getId());
        return true;
    }

    public boolean linkSpouses(UUID aId, UUID bId) {
        Optional<Person> aOpt = repo.findById(aId);
        Optional<Person> bOpt = repo.findById(bId);
        if (aOpt.isEmpty() || bOpt.isEmpty()) return false;
        Person a = aOpt.get();
        Person b = bOpt.get();
        if (a.getSpouseId() != null || b.getSpouseId() != null) return false;
        a.setSpouseId(b.getId());
        b.setSpouseId(a.getId());
        return true;
    }

    public boolean unlinkSpouses(UUID aId, UUID bId) {
        Optional<Person> aOpt = repo.findById(aId);
        Optional<Person> bOpt = repo.findById(bId);
        if (aOpt.isEmpty() || bOpt.isEmpty()) return false;
        Person a = aOpt.get();
        Person b = bOpt.get();
        if (!Objects.equals(a.getSpouseId(), b.getId())) return false;
        a.setSpouseId(null);
        b.setSpouseId(null);
        return true;
    }


    public List<Person> searchByName(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        return repo.findAll().stream()
                .filter(p -> p.getFullName().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(Person::getLastName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Person::getFirstName, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    public Optional<Person> getById(UUID id) {
        return repo.findById(id);
    }

    public Collection<Person> getAll() {
        return repo.findAll();
    }


    public List<List<Person>> buildAncestorsLevels(UUID personId, int maxDepth) {
        List<List<Person>> levels = new ArrayList<>();
        Person start = repo.findById(personId).orElse(null);
        if (start == null) return levels;
        Set<UUID> visited = new HashSet<>();
        List<Person> current = Collections.singletonList(start);
        visited.add(start.getId());
        for (int depth = 0; depth <= maxDepth; depth++) {
            levels.add(current);
            List<Person> next = new ArrayList<>();
            for (Person p : current) {
                for (UUID pid : p.getParentIds()) {
                    if (visited.add(pid)) {
                        repo.findById(pid).ifPresent(next::add);
                    }
                }
            }
            current = next;
            if (current.isEmpty()) break;
        }
        return levels;
    }

    public List<List<Person>> buildDescendantsLevels(UUID personId, int maxDepth) {
        List<List<Person>> levels = new ArrayList<>();
        Person start = repo.findById(personId).orElse(null);
        if (start == null) return levels;
        Set<UUID> visited = new HashSet<>();
        List<Person> current = Collections.singletonList(start);
        visited.add(start.getId());
        for (int depth = 0; depth <= maxDepth; depth++) {
            levels.add(current);
            List<Person> next = new ArrayList<>();
            for (Person p : current) {
                for (UUID cid : p.getChildIds()) {
                    if (visited.add(cid)) {
                        repo.findById(cid).ifPresent(next::add);
                    }
                }
            }
            current = next;
            if (current.isEmpty()) break;
        }
        return levels;
    }


    public Optional<List<Person>> shortestKinshipPath(UUID aId, UUID bId) {
        if (Objects.equals(aId, bId)) {
            return getById(aId).map(Collections::singletonList);
        }
        Map<UUID, UUID> prev = new HashMap<>();
        Deque<UUID> q = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        q.add(aId);
        visited.add(aId);
        while (!q.isEmpty()) {
            UUID cur = q.remove();
            Person p = repo.findById(cur).orElse(null);
            if (p == null) continue;
            List<UUID> neighbors = new ArrayList<>();
            neighbors.addAll(p.getParentIds());
            neighbors.addAll(p.getChildIds());
            if (p.getSpouseId() != null) neighbors.add(p.getSpouseId());
            for (UUID nb : neighbors) {
                if (visited.add(nb)) {
                    prev.put(nb, cur);
                    if (nb.equals(bId)) {
                        // restore path
                        LinkedList<Person> path = new LinkedList<>();
                        UUID x = bId;
                        while (x != null) {
                            UUID pr = prev.get(x);
                            Person node = repo.findById(x).orElse(null);
                            if (node != null) path.addFirst(node);
                            x = pr;
                        }
                        return Optional.of(path);
                    }
                    q.add(nb);
                }
            }
        }
        return Optional.empty();
    }

    public boolean isRelated(UUID aId, UUID bId) {
        return shortestKinshipPath(aId, bId).isPresent();
    }
}
