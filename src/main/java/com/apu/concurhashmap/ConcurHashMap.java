/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author apu
 */
public class ConcurHashMap extends AbstractMap {
    
    private final List<Block> blocks = new ArrayList<>();
    
//    RWLock lock = new RWLock();

    @Override
    public Set entrySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object put(Object key, Object value) {
        //count hash and count blockNumber
        int blockNumber = 0;
        return this.put(blocks.get(blockNumber), key, value);
    }

    @Override
    public Object get(Object key) {
        //count hash and count blockNumber
        int blockNumber = 0;
        return this.get(blocks.get(blockNumber), key);
    }
    
    private Object put(Block block, Object key, Object value) {
        block.lock.lockWrite();
        block.lock.waitReadLockFree();
        
        //make write operation
        /*
            check if(loadFactor>0.75) then {
                we have to block all table, so we set globalLock
            }
            
        */
        
        block.lock.unlockWrite();
        
        return null;
    }
    
    private Object get(Block block, Object key) {
        block.lock.waitWriteLockFree();
        block.lock.lockRead();
        
        //read data
        
        block.lock.unlockRead();
        
        return null;
    }
    
    static class Block {
        List<Integer> hashes = new ArrayList<>();
        RWLock lock = new RWLock();
    }
    
}
