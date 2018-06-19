/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

/**
 *
 * @author apu
 */
public class Logger {
    
    public static void debug(Class cl, String message) {
        System.out.println("Debug: " + cl.getName() + ": " + message);
    }
    
}
