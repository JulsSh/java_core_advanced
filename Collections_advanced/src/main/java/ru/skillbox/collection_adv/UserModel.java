package ru.skillbox.collection_adv;

import java.util.Objects;

public class UserModel {
    private int passportNumber;
    private int age;
    private String userName;

    public UserModel(int passportNumber, int age, String userName) {
        this.passportNumber = passportNumber;
        this.age = age;
        this.userName = userName;
    }

    public int getPassportNumber() {
        return passportNumber;
    }

    public int getAge() {
        return age;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassportNumber(int passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // equals/hashCode можно оставить по паспорту (он уникальный идентификатор)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserModel)) return false;
        UserModel that = (UserModel) o;
        return passportNumber == that.passportNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(passportNumber);
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "passportNumber=" + passportNumber +
                ", age=" + age +
                ", userName='" + userName + '\'' +
                '}';
    }
}

