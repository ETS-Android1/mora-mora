package com.example.hendrik.mianamalaga.container;

public class DictionaryObject {
    private String name;
    private String value;

    public DictionaryObject(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
