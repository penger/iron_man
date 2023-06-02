package org.example;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

//输出和打印kafka中的数据
public class KafkaReaderAndPrint {
    public static void main(String[] args) throws InterruptedException {
        KafkaReaderAndPrint demo = new KafkaReaderAndPrint();
        List<String> list = demo.readFile("e://1.txt");
        Collection<String> collection = demo.skipDuplicateLines(list);
        Map<String, Integer> map = demo.mapReduce(collection);
        demo.printMap(map);
    }

    public void example(String[] args) throws InterruptedException {
        System.out.println("模拟连接kafka并且输出 1000 个数字");
        for (int i = 0; i < 30; i++) {
            Thread.sleep(100);
            System.out.println("current is :"+i);
        }
    }

    //文件读取
    public List<String> readFile(String filePath){
        List<String > list = null;
        if(filePath.isEmpty()){
            System.out.println("需要输入文件地址");
        }else{
            try {
                list = FileUtils.readLines(new File(filePath), Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    //去重
    public Collection<String> skipDuplicateLines(List<String> lines){
        Set<String> set = new HashSet<>();
        for (String line : lines) {
            set.add(line.trim());
        }
        return set;
    }

    // map
    public Map<String,Integer> mapReduce(Collection<String> lines){
        Map<String,Integer> map = new HashMap<>();
        for (String line : lines) {
            String[] info = line.split(" ");
            if(map.containsKey(info[0])){
                int count = map.get(info[0]) + Integer.parseInt(info[1]);
                map.put(info[0],count);
            }else{
                map.put(info[0],Integer.parseInt(info[1]));
            }
        }
        return map;
    }

    public void printMap(Map<String,Integer> map ){
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " -------------> "+ entry.getValue());
        }
    }


}