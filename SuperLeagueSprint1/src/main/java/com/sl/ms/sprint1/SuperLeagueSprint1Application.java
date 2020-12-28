package com.sl.ms.sprint1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static java.nio.file.StandardWatchEventKinds.*;
import com.sl.ms.sprint1.stock.Inventory;
import com.sl.ms.sprint1.stock.InventoryMemoryDatabase;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication()
public class SuperLeagueSprint1Application implements CommandLineRunner {
    @Autowired
    InventoryMemoryDatabase inventoryMemoryDatabase;
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SuperLeagueSprint1Application.class);
        springApplication.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.format("...........SuperLeagueSprint1 - Java 8 Inventory Service Started...........\n");
        String ARCHIVE_LOCATION = "C:/Users/hp/Desktop/SuperLeague/Sprint1/Assignment/SuperLeagueSprint1/src/main/resources/source";
        Path dir = Paths.get(ARCHIVE_LOCATION);
        watchDirectory(dir);
    }

    private void watchDirectory(Path dir) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Map<WatchKey, Path> keyMap = new HashMap<>();

        traversalDirectories(dir,watchService,keyMap);

        for (;;) {

            // try/catch that waits for key to be triggered
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException x) {
                return;
            }

            Path localDir = keyMap.get(key);
            if (localDir == null) {
                System.out.println("Key not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                Path name = ((WatchEvent<Path>)event).context();
                Path child = localDir.resolve(name);

                // print out watched event
                System.out.format("...........%s: %s\n", event.kind().name(), child);
                System.out.format("...........Read Inventory File: %s \n", child);
                readFileAndprintReport(child);

                // if directory is created
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            traversalDirectories(child,watchService,keyMap);
                        }
                    } catch (IOException x) {
                        // do stuff
                    }
                }
            }

            // refine(reset/remove) watchkey map if directory is not valid
            boolean valid = key.reset();
            if (!valid) {
                keyMap.remove(key);

                // all of watch keyMap are inaccessible
                if (keyMap.isEmpty()) {
                    break;
                }
            }
        }

    }
    private void traversalDirectories(final Path start, WatchService watchService, Map<WatchKey, Path> keyMap) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watchService, ENTRY_CREATE);
                keyMap.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void readFileAndprintReport(Path path) {
        //InventoryMemoryDatabase inventoryMemoryDatabase=new InventoryMemoryDatabase();
        List<Inventory> inventoryList = new ArrayList<>();
        try {
            BufferedReader readFile = new BufferedReader(new FileReader(String.valueOf(path)));
            inventoryList = readFile.lines().skip(1).map((line)-> {
                Inventory inventory =new Inventory();
                String[] inventoryData= line.split(",");
                inventory.setId(Integer.parseInt(inventoryData[0]));
                inventory.setName(inventoryData[1]);
                inventory.setPrice(Double.parseDouble(inventoryData[2]));
                inventory.setQuantity(Integer.parseInt(inventoryData[3]));
                inventory.setDate(LocalDate.parse(inventoryData[4]));
                return inventory;
            }).collect(Collectors.toList());

            inventoryMemoryDatabase.setInventoryList(inventoryList);
            printReport(inventoryMemoryDatabase);
            inventoryList.clear();
            readFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printReport(InventoryMemoryDatabase inventoryMemoryDatabase) {
        stockSummaryPerDay(inventoryMemoryDatabase);
        totalStocksSoldToday(inventoryMemoryDatabase);
        top5ItemsForLastMonth(inventoryMemoryDatabase);
        totalStocksSoldPerMonth(inventoryMemoryDatabase);
        quantityOfSalePerStock(inventoryMemoryDatabase);
    }

    public void stockSummaryPerDay(InventoryMemoryDatabase inventoryMemoryDatabase) {
        System.out.println("------------------------Stock Summary Per Day------------------------");

        if (!inventoryMemoryDatabase.getInventoryList().isEmpty()) {

            Map<LocalDate,	List<Inventory>> dateWiseInventoryMap =
                    inventoryMemoryDatabase.getInventoryList().stream()
                            .collect(Collectors.groupingBy(Inventory::getDate));
            dateWiseInventoryMap.entrySet().forEach(i->{
                System.out.format("--------------------Stock Summary Of %s ---------------------\n", i.getKey());
                System.out.println("-----Date    Id     Stock     Price     Quantity ------------");
                i.getValue().forEach(a->{
                    System.out.println(a.toString());
                });
            });
        }
        System.out.println("---------------------------------------------------------------------");
    }

    private void totalStocksSoldToday(InventoryMemoryDatabase inventoryMemoryDatabase) {
        int total = inventoryMemoryDatabase.getInventoryList().stream()
                .filter(i->i.getDate().equals(LocalDate.now()))
                .collect(Collectors.summingInt(i->i.getQuantity()));
        System.out.println("------------------Summary of total items for the day-----------------");
        System.out.println(LocalDate.now() + " : "+total);
        System.out.println("---------------------------------------------------------------------");
    }

    private void top5ItemsForLastMonth(InventoryMemoryDatabase inventoryMemoryDatabase) {
        System.out.println("------------ TOP 5 items in demand. (For last 1 month) --------------");

        Map<Object,	List<Inventory>> dateWiseInventoryMap =
                inventoryMemoryDatabase.getInventoryList().stream()
                        .collect(Collectors.groupingBy(i-> String.valueOf(i.getDate().getYear()) +
                                String.valueOf(i.getDate().getMonthValue())));
        dateWiseInventoryMap.entrySet().forEach(i->{
            System.out.format("-----------------------Top 5 Stock For %s -----------------------\n", i.getKey());
            System.out.println("-----Date    Id     Stock     Price     Quantity ------------");

            List<Inventory> sorted = i.getValue().stream()
            .sorted(Comparator.comparing(Inventory::getQuantity)
                            .reversed()).collect(Collectors.toList());
            sorted.stream().limit(5).forEach(x->{

                System.out.println(x.toString());
            });
        });
        System.out.println("---------------------------------------------------------------------");
    }

    private void totalStocksSoldPerMonth(InventoryMemoryDatabase inventoryMemoryDatabase) {
        System.out.println("--------------- Total Stocks Sold Per Month Summary -----------------");
        Map<Object, Integer> collect=inventoryMemoryDatabase.getInventoryList().stream()
                .collect(Collectors.groupingBy(i-> String.valueOf(i.getDate().getYear()) +
                        String.valueOf(i.getDate().getMonthValue()),
                        Collectors.summingInt(i->i.getQuantity())));
        System.out.println("-------------------Summary of total items by month-------------------");
        collect.entrySet()
                .forEach(i->System.out.print(i.getKey()+" : "+i.getValue()+System.lineSeparator()));

        Map<Object,	List<Inventory>> dateWiseInventoryMap =
                inventoryMemoryDatabase.getInventoryList().stream()
                        .collect(Collectors.groupingBy(i-> String.valueOf(i.getDate().getYear()) +
                                String.valueOf(i.getDate().getMonthValue())));
        dateWiseInventoryMap.entrySet().forEach(i->{
            System.out.format("----------------------Summary For Month %s ----------------------\n", i.getKey());
            System.out.println("-----Date    Id     Stock     Price     Quantity ------------");
            i.getValue().forEach(a->{
                System.out.println(a.toString());
            });
        });
        System.out.println("---------------------------------------------------------------------");
    }

    private void quantityOfSalePerStock(InventoryMemoryDatabase inventoryMemoryDatabase) {
        Map<Object, Integer> collect=inventoryMemoryDatabase.getInventoryList().stream()
                .collect(Collectors.groupingBy(i->i.getName(),Collectors.summingInt(i->i.getQuantity())));
        System.out.println("----------Summary of quantity of sale for one particular item--------");
        collect.entrySet()
                .forEach(i->System.out.print(i.getKey()+" : "+i.getValue()+System.lineSeparator()));
        System.out.println("---------------------------------------------------------------------");
    }

}