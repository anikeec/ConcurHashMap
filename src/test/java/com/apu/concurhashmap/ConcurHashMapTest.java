/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author apu
 */
public class ConcurHashMapTest {
    
    public ConcurHashMapTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of entrySet method, of class ConcurHashMap.
     */
//    @Test
//    public void testEntrySet() {
//        System.out.println("entrySet");
//        ConcurHashMap instance = new ConcurHashMap();
//        Set expResult = null;
//        Set result = instance.entrySet();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of put method, of class ConcurHashMap.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        String key = "testKey";
        String value = "testValue";
        ConcurHashMap<String, String> instance = new ConcurHashMap<>();
        String expResult = null;
        String result = instance.put(key, value);
        assertEquals(expResult, result);
        
        result = instance.put(key, "test2Value");
        assertEquals(value, result);
    }

    /**
     * Test of get method, of class ConcurHashMap.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        String key = "key";
        String value = "value";
        ConcurHashMap<String, String> instance = new ConcurHashMap<>();
        instance.put(key, value);
        String expResult = "value";
        String result = instance.get(key);
        assertEquals(expResult, result);
    }
    
}
