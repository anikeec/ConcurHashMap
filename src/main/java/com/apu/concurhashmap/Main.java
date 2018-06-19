/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author apu
 */
public class Main {
    
    static final int MAX_TEST = 100000;
    
    public static void main(String[] args) {
        Map<Integer, Integer> mapEt = new HashMap();
        final Map<Integer, Integer> map0 = new ConcurHashMap<>();
        final Map<Integer, Integer> map1 = new ConcurHashMap<>();
        final Map<Integer, Integer> map2 = new ConcurrentHashMap();
                        //Collections.synchronizedMap(new HashMap());
        final Map<Integer, Integer> map3 = new Hashtable<>();
                        //Collections.synchronizedMap(new HashMap());
        List<Thread> threads = new ArrayList<>();        
        
        final int THREAD_AMOUNT = 10;        
        final int[][] inputData1 = new int[THREAD_AMOUNT][MAX_TEST];
        final int[][] inputData2 = new int[THREAD_AMOUNT][MAX_TEST];
        int start = 0;
        for(int threadNumber=0; threadNumber<THREAD_AMOUNT; threadNumber++) {
            start += MAX_TEST;
            for(int ptr=0; ptr<MAX_TEST; ptr++) {                
                Double dbl = Math.random()*(ptr+start);
                int m = dbl.intValue();
                int k = (m%start) + start;
//                int k = ptr + start;
                inputData1[threadNumber][ptr] = k;
                inputData2[threadNumber][ptr] = k;
                mapEt.put(k, k);
            }
        }  
        System.out.println("mapSourceSize: " + mapEt.size());
        
        //test1
        CountDownLatch latchStart = new CountDownLatch(THREAD_AMOUNT);
        CountDownLatch latchFinish = new CountDownLatch(THREAD_AMOUNT);
        long startTime = System.currentTimeMillis();
        for(int i=0; i<THREAD_AMOUNT; i++) {
            Thread thread = new Thread(
                new TestHashTable(latchStart, latchFinish, inputData1, map0, i));
            thread.setName("t1_" + i);
            threads.add(thread);
            thread.start();
        } 
        try {
            latchFinish.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        long finishTime = System.currentTimeMillis();
        System.out.println(finishTime - startTime + " ms, map0: " + map0.size());

        //test2
        latchStart = new CountDownLatch(THREAD_AMOUNT);
        latchFinish = new CountDownLatch(THREAD_AMOUNT);
        startTime = System.currentTimeMillis();
        for(int i=0; i<THREAD_AMOUNT; i++) {
            Thread thread = new Thread(
                new TestHashTable(latchStart, latchFinish, inputData1, map3, i));
            thread.setName("t2_" + i);
            threads.add(thread);
            thread.start();
        } 
        try {
            latchFinish.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        finishTime = System.currentTimeMillis();
        System.out.println(finishTime - startTime + " ms, map3: " + map3.size());
    }
    
    static class TestHashTable implements Runnable {
        
        private int multiplier;
        private volatile Map<Integer,Integer> map;
        private CountDownLatch latchStart;
        private CountDownLatch latchFinish;
        private int[][] inputData;
        private volatile int read;
        private volatile Integer read1;
        private volatile Integer read2;
        private List<Integer> failList = new ArrayList<>();

        public TestHashTable(CountDownLatch latchStart,
                            CountDownLatch latchFinish,
                            int[][] inputData, 
                            Map map, 
                            int multiplier) {
            this.latchStart = latchStart;
            this.latchFinish = latchFinish;
            this.inputData = inputData;
            this.map = map;
            this.multiplier = multiplier;
        }

        @Override
        public void run() {
            int amount = 0;
            int amountDel = 0;
            latchStart.countDown();
            try {
                latchStart.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(int i=0;i<MAX_TEST;i++) {
                int insertData = inputData[multiplier][i];
                try {
                synchronized(this) {
                    read1 = map.put(insertData, insertData);
                    read1 = insertData;              
                    read2 = insertData;
                    read = map.get(read2);
                }                              
//                    if(read != insertData) {
//                        Logger.debug(this.getClass(),
//    Thread.currentThread().getName() + "_fail: " + insertData + ". - " + amount);
//                    }
                    synchronized(this) {
//                        if((read%MAX_TEST != 0)&&(map.get(read-1) != null)) {
//                            map.remove(read-1);
//                            amountDel++;
//                            read1 = map.put(read-1, read-1);
//                        }
                    }                
                } catch(Exception e) {
                    com.apu.concurhashmap.Logger.debug(this.getClass(),
                        Thread.currentThread().getName() + ": " + 
                        e.getMessage() + ", key: " + insertData + ". - " + amount);
                    failList.add(insertData);
                    amount++;
                }
            }
            if(!failList.isEmpty()) {
                System.out.print("Check: ");
                for(int key:failList) {
                    System.out.print("key:" + key + ", ret:" + map.get(key) + ", ");
                }
                System.out.println("");
            }
//            Logger.debug(this.getClass(),
//                Thread.currentThread().getName() + ": amountDel = " + amountDel);
            latchFinish.countDown();
        }
        
    }
    
}
