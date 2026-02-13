package com.model;


public class Album {
    public Album(String name, Double i) {
        this.name = name;
        this.sales = i;
    }
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Double sales; //Поле не может быть null, Значение поля должно быть больше 0

    public String getName() {
        return name;
    }

    public Double getSales() {
        return sales;
    }

    @Override
    public String toString() {
        return name + " (" + sales + " sales)";
    }
}
