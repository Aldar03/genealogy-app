package org.example.genealogy.view;

import org.example.genealogy.model.Gender;
import org.example.genealogy.model.Person;
import org.example.genealogy.service.GenealogyService;
import org.example.genealogy.service.TreePrinter;
import org.example.genealogy.storage.JsonStorage;
import org.example.genealogy.storage.Repository;

import java.time.LocalDate;
import java.util.*;

public class Main {

    private final Scanner scanner = new Scanner(System.in);
    private final Repository repo = new Repository();
    private final GenealogyService service = new GenealogyService(repo);
    private final JsonStorage storage = new JsonStorage();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("Генеалогическое древо");
        seedDemoData();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            String choiceLC = choice.toLowerCase(Locale.ROOT);
            try {
                switch (choice) {
                    case "1":
                        addPerson();
                        break;
                    case "2":
                        linkRelations();
                        break;
                    case "3":
                        editPerson();
                        break;
                    case "4":
                        deletePerson();
                        break;
                    case "5":
                        searchPeople();
                        break;
                    case "6":
                        buildAncestors();
                        break;
                    case "7":
                        buildDescendants();
                        break;
                    case "8":
                        checkRelated();
                        break;
                    case "9":
                        saveToFile();
                        break;
                    case "10":
                        loadFromFile();
                        break;
                    case "11":
                        listAll();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        switch (choiceLC) {
                            case "listall":
                                listAll();
                                break;
                            case "add":
                                addPerson();
                                break;
                            case "exit":
                            case "quit":
                                running = false;
                                break;
                            default:
                                System.out.println("Неизвестная команда");
                        }
                }
            } catch (Exception ex) {
                System.out.println("Ошибка: " + ex.getMessage());
            }
        }
        System.out.println("Выход.");
    }

    private void printMenu() {
        System.out.println("\nМеню:");
        System.out.println("1) Добавить человека");
        System.out.println("2) Установить родственные связи");
        System.out.println("3) Редактировать данные");
        System.out.println("4) Удалить человека");
        System.out.println("5) Поиск по ФИО");
        System.out.println("6) Дерево предков");
        System.out.println("7) Дерево потомков");
        System.out.println("8) Проверить родственную связь и степень");
        System.out.println("9) Сохранить в файл (JSON)");
        System.out.println("10) Загрузить из файла (JSON)");
        System.out.println("11) Показать всех");
        System.out.println("0) Выход");
        System.out.print("> ");
    }

    private void addPerson() {
        System.out.println("Фамилия:");
        String last = scanner.nextLine().trim();
        System.out.println("Имя:");
        String first = scanner.nextLine().trim();
        System.out.println("Отчество (можно пусто):");
        String middle = scanner.nextLine().trim();
        LocalDate birth = readDate("Дата рождения (YYYY-MM-DD):");
        LocalDate death = readOptionalDate("Дата смерти (YYYY-MM-DD, пусто если жив):");
        Gender gender = readGender();

        Person p = service.addPerson(last, first, middle.isEmpty() ? null : middle, birth, death, gender);
        System.out.println("Добавлено: " + p + "  (ID=" + p.getId() + ")");
    }

    private void linkRelations() {
        System.out.println("1) Родитель-ребенок  2) Супруги");
        String t = scanner.nextLine().trim();
        if ("1".equals(t)) {
            UUID parent = readPersonId("ID родителя:");
            UUID child = readPersonId("ID ребенка:");
            boolean ok = service.linkParentChild(parent, child);
            System.out.println(ok ? "Связь установлена" : "Не удалось установить связь");
        } else if ("2".equals(t)) {
            UUID a = readPersonId("ID супруга A:");
            UUID b = readPersonId("ID супруга B:");
            boolean ok = service.linkSpouses(a, b);
            System.out.println(ok ? "Супруги связаны" : "Не удалось связать супругов (возможно, уже состоят в браке)");
        } else {
            System.out.println("Неизвестный тип");
        }
    }

    private void editPerson() {
        UUID id = readPersonId("ID человека для редактирования:");
        System.out.println("Новые данные (оставьте пустым, чтобы не менять):");
        System.out.println("Фамилия:");
        String last = emptyToNull(scanner.nextLine().trim());
        System.out.println("Имя:");
        String first = emptyToNull(scanner.nextLine().trim());
        System.out.println("Отчество:");
        String middle = emptyToNull(scanner.nextLine().trim());
        LocalDate birth = readOptionalDate("Дата рождения (YYYY-MM-DD):");
        LocalDate death = readOptionalDate("Дата смерти (YYYY-MM-DD):");
        Gender gender = readOptionalGender();

        boolean ok = service.editPerson(id, last, first, middle, birth, death, gender);
        System.out.println(ok ? "Обновлено" : "Человек не найден");
    }

    private void deletePerson() {
        UUID id = readPersonId("ID для удаления:");
        boolean ok = service.deletePerson(id);
        System.out.println(ok ? "Удалено" : "Человек не найден");
    }

    private void searchPeople() {
        System.out.println("Введите ФИО или часть:");
        String q = scanner.nextLine().trim();
        var list = service.searchByName(q);
        if (list.isEmpty()) {
            System.out.println("Ничего не найдено");
        } else {
            list.forEach(p -> System.out.println(p + "  (ID=" + p.getId() + ")"));
        }
    }

    private void buildAncestors() {
        UUID id = readPersonId("ID человека:");
        int depth = readInt("Глубина (0..10):", 10);
        var levels = service.buildAncestorsLevels(id, depth);
        TreePrinter.printLevels(levels, "Дерево предков");
    }

    private void buildDescendants() {
        UUID id = readPersonId("ID человека:");
        int depth = readInt("Глубина (0..10):", 10);
        var levels = service.buildDescendantsLevels(id, depth);
        TreePrinter.printLevels(levels, "Дерево потомков");
    }

    private void checkRelated() {
        UUID a = readPersonId("ID A:");
        UUID b = readPersonId("ID B:");
        var pathOpt = service.shortestKinshipPath(a, b);
        if (pathOpt.isPresent()) {
            var path = pathOpt.get();
            System.out.println("Связаны. Рёбер в кратчайшем пути: " + (path.size() - 1));
            for (int i = 0; i < path.size(); i++) {
                String prefix = (i == 0) ? "Начало: " : (i == path.size() - 1) ? "Конец:  " : " → ";
                System.out.println(prefix + path.get(i) + " (ID=" + path.get(i).getId() + ")");
            }
        } else {
            System.out.println("Связь не найдена.");
        }
    }

    private void saveToFile() {
        System.out.println("Путь к JSON-файлу для сохранения:");
        String path = scanner.nextLine().trim();
        try {
            storage.save(path, service.getAll());
            System.out.println("Сохранено: " + path);
        } catch (Exception e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        System.out.println("Путь к JSON-файлу для загрузки:");
        String path = scanner.nextLine().trim();
        try {
            var list = storage.load(path);
            repo.clear();
            repo.saveAll(list);
            System.out.println("Загружено записей: " + list.size());
        } catch (Exception e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void listAll() {
        var all = service.getAll();
        if (all.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }
        for (Person p : all) {
            System.out.println(p + "  (ID=" + p.getId() + ")");
        }
    }


    private UUID readPersonId(String prompt) {
        listAll();
        System.out.println(prompt);
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException e) {
                System.out.println("Некорректный UUID. Повторите:");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.println(prompt);
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                System.out.println("Некорректная дата. Формат YYYY-MM-DD. Повторите:");
            }
        }
    }

    private LocalDate readOptionalDate(String prompt) {
        System.out.println(prompt);
        while (true) {
            String s = scanner.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                System.out.println("Некорректная дата. Формат YYYY-MM-DD. Повторите или оставьте пусто:");
            }
        }
    }

    private Gender readGender() {
        System.out.println("Пол (М/Ж):");
        while (true) {
            String s = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
            switch (s) {
                case "М":
                    return Gender.MALE;
                case "Ж":
                    return Gender.FEMALE;

                default:
                    System.out.println("Введите М, Ж:");
            }
        }
    }

    private Gender readOptionalGender() {
        System.out.println("Пол (М/Ж, пусто если без изменений):");
        while (true) {
            String s = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
            if (s.isEmpty()) return null;
            switch (s) {
                case "М":
                    return Gender.MALE;
                case "Ж":
                    return Gender.FEMALE;
                default:
                    System.out.println("Введите M, Ж:");
            }
        }
    }

    private int readInt(String prompt, int max) {
        System.out.println(prompt);
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < 0 || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Некорректное число. Повторите:");
            }
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private void seedDemoData() {
        var ivan = service.addPerson("Басангов", "Николай", "Додлаевич", LocalDate.of(1949, 1, 5), null, Gender.MALE);
        var maria = service.addPerson("Басангова", "Галина", "Сангаджиева", LocalDate.of(1951, 3, 2), null, Gender.FEMALE);
        var petr = service.addPerson("Басангов", "Борис", "Николаевич", LocalDate.of(1978, 6, 12), null, Gender.MALE);
        var olga = service.addPerson("Басангов", "Данил", "Николаевич", LocalDate.of(1986, 11, 23), null, Gender.MALE);
        var nina = service.addPerson("Басангова", "Гиляна", "Николаевна", LocalDate.of(1974, 2, 1), null, Gender.FEMALE);

        service.linkSpouses(ivan.getId(), maria.getId());
        service.linkParentChild(ivan.getId(), petr.getId());
        service.linkParentChild(maria.getId(), petr.getId());
        service.linkSpouses(petr.getId(), olga.getId());
        service.linkParentChild(petr.getId(), nina.getId());
        service.linkParentChild(olga.getId(), nina.getId());
    }
}
