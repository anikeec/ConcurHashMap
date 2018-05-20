/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author apu
 */
public class ConcurHashMap<K,V> extends AbstractMap<K,V> {
    
    private final int INIT_BLOCK_SIZE = 8;
    private final int INIT_BLOCKS_AMOUNT = 2;
    private final int BLOCK_SIZE_MULT = 2;
    private final int BLOCKS_AMOUNT_MULT = 2;
    private final List<Block<K,V>> blocks = new ArrayList<>();    
    private final GlobalLock globalLock = new GlobalLock();
    private int blockSize = INIT_BLOCK_SIZE;
    private int blocksAmount = INIT_BLOCKS_AMOUNT;
    private final float LOAD_FACTOR_DEFAULT = 0.75f;    

    public ConcurHashMap() {
        for(int i=0; i< this.blocksAmount; i++) {
            blocks.add(new Block<>());
        }
    }

    @Override
    public Set entrySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public V put(K key, V value) {
        if(key == null)
            throw new IllegalArgumentException();
        globalLock.waitLockFree();
        return this.put(getBlock(key), key, value);
    }

    @Override
    public V get(Object key) {
        if(key == null)
            throw new IllegalArgumentException();
        globalLock.waitLockFree();        
        V ret = this.get(getBlock(key), key);
        return ret;
    }
    
    private V put(BlockPtr blockPtr, K key, V value) {
        V retValue = null;
        Block block = blockPtr.block;
        int index = blockPtr.index;
        
        block.lock.lockWrite();
        block.lock.waitReadLockFree();
        
        //make write operation
        Node<K,V> node = new Node<>(key.hashCode(), key, value, null);
        Node<K,V> firstNode = block.table[index];
        if(firstNode == null) {
            block.table[index] = node;
//            block.lastNode = block.firstNode;
            block.table[index].length++;
        } else {
            Node<K,V> nodeTemp = firstNode;
            Node<K,V> nodeFinded = null;
            if(nodeTemp.next == null) {
                if(nodeTemp.key.equals(key)) {
                    nodeFinded = nodeTemp;
                }
            } else {
                while(nodeTemp.next != null) {
                    if(nodeTemp.key.equals(key)) {
                        nodeFinded = nodeTemp;
                        break;
                    }
                    nodeTemp = nodeTemp.next;
                } 
            }
            if(nodeFinded != null) {
                retValue = nodeFinded.value;
                nodeFinded.value = value;
            } else {
//                block.lastNode.next = node;
                nodeTemp.next = node;
//                block.lastNode = node;
                firstNode.length++;
            } 
            
        }
        
        block.lock.unlockWrite();
        
        refreshTable();
        
        return retValue;
    }
    
    private V get(BlockPtr blockPtr, Object key) {
        V retValue = null;
        Block block = blockPtr.block;
        int index = blockPtr.index;
        
        block.lock.waitWriteLockFree();
        block.lock.lockRead();
        
        if(block.table[index] == null)
            retValue = null;        
        
        Node<K,V> node = block.table[index];
        if(node == null) {
            retValue = null;
        } else if(node.next == null) {
            if(node.key.equals(key)) {
                retValue = node.value;
            }
        } else {
            while(node.next != null) {
                if(node.key.equals(key)) {
                    retValue = node.value;
                    break;
                }
                node = node.next;
            } 
        }
        
        block.lock.unlockRead();
        
        return retValue;
    }  
        
    private void refreshTable() {
        int fullSize = 0;
        for(Block block:blocks) {
            for(int i=0;i<blockSize; i++) {
                if(block.table[i] != null)
                    fullSize += block.table[i].length;
            }
        }
        int hashTableSize = blockSize * blocksAmount;
        float loadFactorTemp = (float)hashTableSize / fullSize;
        if(loadFactorTemp < LOAD_FACTOR_DEFAULT) { 
            System.out.println("fullSize:" + fullSize + ", loadFactor:" + loadFactorTemp);
//            globalLock.lock();
//            //wait until other locks will finish their works
//            int newBlockSize = blockSize * BLOCK_SIZE_MULT;
//            int newBlocksAmount = blocksAmount * BLOCKS_AMOUNT_MULT;
//            
//            //we have to remake table          
//            
//            globalLock.unlock();           
        }            
    }
    
    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (key.hashCode() & 0x7FFFFFFF);
    }
    
    private BlockPtr getBlock(Object key) {
        int hash = hash(key);
        int globalTablelPtr = hash%(blocksAmount*blockSize);
        int blockNumber = globalTablelPtr/blockSize;
        int blockTablePtr = globalTablelPtr - (blockNumber * blockSize);         
        return new BlockPtr(blocks.get(blockNumber), blockTablePtr);
    }
    
    private class Block<K,V> {
        Node<K,V>[] table = new Node[blockSize];
        RWLock lock = new RWLock(globalLock);
    }
    
    private class BlockPtr {
        Block block;
        int index;
        
        public BlockPtr(Block blockPtr, int indexPtr) {
            this.block = blockPtr;
            this.index = indexPtr;
        }        
    }
    
    private static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;
        int length;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public final K getKey()        { return key; }
        
        @Override
        public final V getValue()      { return value; }
        
        @Override
        public final String toString() { return key + "=" + value; }

        @Override
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
    
}
