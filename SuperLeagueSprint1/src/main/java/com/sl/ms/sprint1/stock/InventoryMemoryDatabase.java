package com.sl.ms.sprint1.stock;

import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class InventoryMemoryDatabase {
    private List<Inventory> databaseInventoryList=new ArrayList<>();

    public List<Inventory> getInventoryList() {
        return databaseInventoryList;
    }

    public void setInventoryList(List<Inventory> inventoryList) {
        databaseInventoryList.addAll(inventoryList);
    }

}