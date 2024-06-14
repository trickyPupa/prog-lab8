package common.model.entities;

import common.abstractions.IInputManager;
import common.abstractions.IOutputManager;
import common.exceptions.InterruptException;
import common.model.enums.Country;
import common.model.enums.EyeColor;
import common.model.enums.HairColor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.lang.Math.max;

public class Person implements Comparable<Person>, Checkable {
    private static int maxNameLen = 10;

    private int id = 0;
    private String name; //Поле не может быть null, Строка не может быть пустой
//    @JsonSerialize(using = FileManager.CustomDateSerializer.class)
    private LocalDate birthday; //Поле не может быть null
    private EyeColor eyeColor; //Поле может быть null
    private HairColor hairColor; //Поле не может быть null
    private Country nationality; //Поле не может быть null
    private Location location; //Поле не может быть null

    public Person() {}

    public Person(String name, LocalDate birthday, EyeColor eyeColor, HairColor hairColor, Country nationality, Location location) {
        this.name = name;
        this.birthday = birthday;
        this.eyeColor = eyeColor;
        this.hairColor = hairColor;
        this.nationality = nationality;
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setEyeColor(EyeColor eyeColor) {
        this.eyeColor = eyeColor;
    }

    public void setHairColor(HairColor hairColor) {
        this.hairColor = hairColor;
    }

    public void setNationality(Country nationality) {
        this.nationality = nationality;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public EyeColor getEyeColor() {
        return eyeColor;
    }

    public HairColor getHairColor() {
        return hairColor;
    }

    public Country getNationality() {
        return nationality;
    }

    public Location getLocation() {
        return location;
    }

    public static Person createPerson(IInputManager input, IOutputManager output){
        Person elem = new Person();

        Map<String, Predicate<String>> args_checkers = new LinkedHashMap<>();
        args_checkers.put("имя режиссёра", x -> {
            if (!x.isBlank()){
                elem.setName(x);
                return true;
            }
            return false;
        });
        args_checkers.put("дату рождения режиссёра в формате ДД.ММ.ГГГГ", x -> {
            if (x.matches("(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.(1[89]\\d\\d|20([01]\\d|2[01234]))")){

                String[] temp = x.split("\\.");

                int day = Integer.parseInt(temp[0].strip());
                int month = Integer.parseInt(temp[1].strip());
                int year = Integer.parseInt(temp[2].strip());

                elem.setBirthday(LocalDate.of(year, month, day));
                return true;
            }
            return false;
        });
        args_checkers.put("цвет глаз режиссёра (BLUE, YELLOW, ORANGE, WHITE, BROWN)", x -> {
            if (EyeColor.contains(x.toUpperCase())){
                elem.setEyeColor(EyeColor.valueOf(x.toUpperCase()));
                return true;
            }
            return false;
        });
        args_checkers.put("цвет волос режиссёра (GREEN, RED, BLUE, YELLOW, ORANGE)", x -> {
            if (HairColor.contains(x.toUpperCase())){
                elem.setHairColor(HairColor.valueOf(x.toUpperCase()));
                return true;
            }
            return false;
        });
        args_checkers.put("национальность режиссёра (FRANCE, INDIA, VATICAN, THAILAND)", x -> {
            if (Country.contains(x.toUpperCase())){
                elem.setNationality(Country.valueOf(x.toUpperCase()));
                return true;
            }
            return false;
        });

        try{
            for (String a : args_checkers.keySet()){
                Predicate<String> check = args_checkers.get(a);
                output.print("Введите " + a + ":");
                String line = input.nextLine();
                if (line == null || line.strip().equals("exit")){
                    throw new InterruptException();
                }

                while (!check.test(line)){
                    output.print("Некорректные данные.");
                    output.print("Введите " + a + ":");
                    line = input.nextLine();
                }
            }

            elem.setLocation(Location.createLocation(input, output));

            if (elem.name.length() > maxNameLen)
                maxNameLen = elem.name.length();
            return elem;
        } catch (IOException e){
            output.print("Что-то случилось, введите команду заново.");
            throw new InterruptException();
        }
    }

    public static Person createPersonNoText(IInputManager input, IOutputManager output){
        Person elem = new Person();

        Map<String, Predicate<String>> args_checkers = new LinkedHashMap<>();
        args_checkers.put("имя режиссёра", x -> {
            if (!x.isBlank()){
                elem.setName(x);
                return true;
            }
            return false;
        });
        args_checkers.put("дату рождения режиссёра в формате ДД.ММ.ГГГГ", x -> {
            if (x.matches("(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.(1[89]\\d\\d|20([01]\\d|2[01234]))")){

                String[] temp = x.split("\\.");

                int day = Integer.parseInt(temp[0].strip());
                int month = Integer.parseInt(temp[1].strip());
                int year = Integer.parseInt(temp[2].strip());

                elem.setBirthday(LocalDate.of(year, month, day));
                return true;
            }
            return false;
        });
        args_checkers.put("цвет глаз режиссёра (BLUE, YELLOW, ORANGE, WHITE, BROWN)", x -> {
            if (EyeColor.contains(x.toUpperCase())){
                elem.setEyeColor(EyeColor.valueOf(x.toUpperCase()));
                return true;
            }
            return false;
        });
        args_checkers.put("цвет волос режиссёра (GREEN, RED, BLUE, YELLOW, ORANGE)", x -> {
            if (HairColor.contains(x.toUpperCase())){
                elem.setHairColor(HairColor.valueOf(x.toUpperCase()));
                return true;
            }
            return false;
        });
        args_checkers.put("национальность режиссёра (FRANCE, INDIA, VATICAN, THAILAND)", x -> {
            if (Country.contains(x.toUpperCase())){
                elem.setNationality(Country.valueOf(x.toUpperCase()));
                return true;
            }
            return false;
        });

        try{
            for (String a : args_checkers.keySet()){
                Predicate<String> check = args_checkers.get(a);
                output.print("Введите " + a + ":\n");
                String line = input.nextLine();
                output.print(line + "\n");
                if (line == null || line.strip().equals("exit")){
                    throw new InterruptException();
                }

                while (!check.test(line)){
                    output.print("Некорректные данные.\n");
                    output.print("Введите " + a + ":\n");
                    line = input.nextLine();
                    output.print(line + "\n");
                }
            }

            elem.setLocation(Location.createLocationNoText(input, output));

            if (elem.name.length() > maxNameLen)
                maxNameLen = elem.name.length();
            return elem;
        } catch (IOException e){
            output.print("Что-то случилось, введите команду заново.\n");
            throw new InterruptException();
        }
    }

    @Override
    public boolean checkItself(){
        return name != null && !name.isBlank() && birthday != null && location.checkItself();
    }

    @Override
    public String toString() {
        return String.format("%s (%8s), born in %s", " ".repeat(max(maxNameLen - name.length(), 0)) + name,
                nationality.name(), birthday);
    }

    @Override
    public int compareTo(Person o) {
        return !Objects.equals(this.name, o.name) ? this.name.toLowerCase().compareTo(o.name.toLowerCase()) : -this.birthday.compareTo(o.birthday);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return Objects.equals(name, person.name) && Objects.equals(birthday, person.birthday)
                && eyeColor == person.eyeColor && hairColor == person.hairColor
                && nationality == person.nationality && Objects.equals(location, person.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthday, eyeColor, hairColor, nationality, location);
    }
}
