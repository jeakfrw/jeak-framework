package de.fearnixx.jeak.controller.testImpls;

public class DummyObject {
    private String name;
    private int age;

    public DummyObject() {
    }

    public DummyObject(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
