package com.sl.ms.sprint1.stock;

import java.time.LocalDate;

public class Inventory {

    private int Id;
    private String name;
    private Double price;
    private int quantity;
    private LocalDate date;

    public int getId() {
        return Id;
    }
    public void setId(int id) {
        Id = id;
    }

    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String space="    ";
        return this.date+space+this.Id +space +this.name+space +this.price+space+this.quantity;
    }
}