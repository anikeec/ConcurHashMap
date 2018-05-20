/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author apu
 */
public class Main {
    
    public static void main(String[] args) {
        Map map = new ConcurHashMap();
        List<Thread> threads = new ArrayList<>();
        for(int i=0; i<10; i++) {
            Thread thread = new Thread(new TestHashTable(map, i));
            threads.add(thread);
        }
        for(Thread thread : threads) {
            thread.start();
        }
        for(Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(System.currentTimeMillis() + ", " + map.size());
        System.out.println(map.size());
    }
    
    static class TestHashTable implements Runnable {
        
        private int multiplier;
        private Map map;

        public TestHashTable(Map map, int multiplier) {
            this.map = map;
            this.multiplier = multiplier;
        }

        @Override
        public void run() {
            int start = 100000*multiplier;
            for(int i=start; i<start+4000; i++) {
                map.put("thread" + i, null);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
}
