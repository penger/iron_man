package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SleepTask {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("sleep start at : "+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        long time = Long.parseLong(args[0]);
        Thread.sleep(time);
        System.out.println("sleep :"+time);
        System.out.println("sleep end at : "+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
