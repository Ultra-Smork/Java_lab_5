package com.model;



public class Coordinates {
    public Coordinates(long x, int y) {
        this.x = x;
            if (x > 554) {
                throw new IllegalArgumentException("X cannot be greater than 554");
            }
        this.y = y;
            if (y > 782) {
                throw new IllegalArgumentException("Y cannot be greater than 782");
            }
    }
    private Long x; //Максимальное значение поля: 554, Поле не может быть null
    private Integer y; //Максимальное значение поля: 782, Поле не может быть null

    public Long getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
