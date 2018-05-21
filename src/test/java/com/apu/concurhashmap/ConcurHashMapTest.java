/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
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
    
    private ConcurHashMap<String, String> instance;
    
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
        instance = new ConcurHashMap<>();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of entrySet method, of class ConcurHashMap.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySet() {
        System.out.println("entrySet");
        Set result = instance.entrySet();
    }

    /**
     * Test of put method, of class ConcurHashMap.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        String key = "testKey";
        String value = "testValue";
        String result = instance.put(key, value);
        assertEquals(null, result);        
        result = instance.put(key, "test2Value");
        assertEquals(value, result);
    }

    /**
     * Test of get method, of class ConcurHashMap.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        String key = "testKey";
        String value = "testValue";
        instance.put(key, value);
        String result = instance.get(key);
        assertEquals(value, result);
    }

    /**
     * Test of remove method, of class ConcurHashMap.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        String key = "testKey1";
        String value = "testValue1";
        instance.put(key, value);
        key = "testKey2";
        value = "testValue2";
        instance.put(key, value);
        key = "testKey3";
        value = "testValue3";
        instance.put(key, value);
        assertTrue(instance.size() == 3);        
        String result = instance.remove(key);
        assertEquals(value, result);
    }

    /**
     * Test of size method, of class ConcurHashMap.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        assertTrue(instance.isEmpty()); 
        String key = "testKey1";
        String value = "testValue1";
        instance.put(key, value);
        key = "testKey2";
        value = "testValue2";
        instance.put(key, value);
        assertTrue(instance.size() == 2);        
    }   

    /**
     * Test of getAllAsList method, of class ConcurHashMap.
     */
    @Test
    public void testGetAllAsList() {
        System.out.println("getAllAsList");
        String key = "testKey1";
        String value = "testValue1";
        instance.put(key, value);
        key = "testKey2";
        value = "testValue2";
        instance.put(key, value);
        List result = instance.getAllAsList();
        assertTrue(result.size() == 2);
    }

    /**
     * Test of forEach method, of class ConcurHashMap.
     */
    @Test
    public void testForEach() {
        System.out.println("forEach");
        String key = "testKey1";
        String value = "testValue1";
        instance.put(key, value);
        key = "testKey2";
        value = "testValue2";
        instance.put(key, value);
        instance.forEach((k,v) -> System.out.println("key: " + k + ", value: " + v));
        assertTrue(true);
    }
    
}
