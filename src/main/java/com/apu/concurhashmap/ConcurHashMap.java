/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 *
 * @author apu
 */
public class ConcurHashMap<K,V> extends AbstractMap<K,V> {
    
    private static final int INIT_BLOCK_SIZE = 8;
    private static final int INIT_BLOCKS_AMOUNT = 2;
    private final int BLOCK_SIZE_MULT = 2;
    private final int BLOCKS_AMOUNT_MULT = 2;
    private final float LOAD_FACTOR_DEFAULT = 0.75f;    
    
    private Block<K,V>[] blocks;    
    private final GlobalLock globalLock = new GlobalLock();
    private int blockSize;
    private int blocksAmount;    

    transient int modCount;
    
    public ConcurHashMap() {
        this(INIT_BLOCK_SIZE, INIT_BLOCKS_AMOUNT);
    }
    
    public ConcurHashMap(int blockSize, int blocksAmount) {
        this.blockSize = blockSize;
        this.blocksAmount = blocksAmount;
        blocks = new Block[blocksAmount];
        for(int i=0; i< this.blocksAmount; i++) {
            blocks[i] = new Block<>(blockSize, this.globalLock);
        }
    }

    @Override
    public Set entrySet() {
        throw new UnsupportedOperationException("Method has not realized yet");
    }
    
    public List<Node<K,V>> getAllAsList() {
        List<Node<K,V>> list = new ArrayList<>();
        for(Block block:blocks) {
            for(int i=0;i<blockSize; i++) {
                if(block.table[i] != null) {
                    Node nodeTemp = block.table[i];
                    list.add(nodeTemp);
                    while(nodeTemp.next != null) {
                        nodeTemp = nodeTemp.next;
                        list.add(nodeTemp);                        
                    }
                }
            }
        }
        return list;
    }
    
    @Override
    public V get(Object key) {
        if(key == null)
            throw new IllegalArgumentException();
        globalLock.waitLockFree();        
        V ret = this.get(getBlock(key), key);
        return ret;
    }
    
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null)
            throw new NullPointerException();
//        if (!blocks.isEmpty()) {
            int mc = modCount;
            List<Node<K,V>> list = this.getAllAsList();
            for (Node<K,V> node : list) {
                action.accept(node.key, node.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
//        }
    }
    
    @Override
    public V put(K key, V value) {
        if(key == null)
            throw new IllegalArgumentException();
        globalLock.waitLockFree();
        return this.put(getBlock(key), key, value);
    }
    
    @Override
    public V remove(Object key) {
        if(key == null)
            throw new IllegalArgumentException();
        globalLock.waitLockFree();
        return this.remove(getBlock(key), key);
    }
    
    @Override
    public int size() {
        int fullSize = 0;
        for(Block block:blocks) {
            for(int i=0;i<blockSize; i++) {
                if(block.table[i] != null)
                    fullSize += block.table[i].length;
            }
        }
        return fullSize;
    }

    private V get(BlockPtr blockPtr, Object key) {
        V retValue = null;
        Block block = blockPtr.block;
        int index = blockPtr.index;
//        System.out.println(Thread.currentThread().getName() + " - rdB" + blockPtr.blockId);
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
    
    private V put(BlockPtr blockPtr, K key, V value) {
        V retValue = null;
        Block block = blockPtr.block;
        int index = blockPtr.index;
//        System.out.println(Thread.currentThread().getName() + " - wrB" + blockPtr.blockId);
        block.lock.lockWrite();
        
        Node<K,V> node = new Node<>(key.hashCode(), key, value, null);
        Node<K,V> firstNode = block.table[index];
        if(firstNode == null) {
            block.table[index] = node;
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
                nodeTemp.next = node;
                firstNode.length++;
            }             
        }
        
        block.lock.unlockWrite();
        
//        refreshTable();
        
        ++modCount;
        
        return retValue;
    }

    private V remove(BlockPtr blockPtr, Object key) {        
        Block block = blockPtr.block;
        int index = blockPtr.index;
        
        block.lock.lockWrite();
        
        V retValue = null;
        Node<K,V> node = block.table[index];
        if(node != null) {
            Node<K,V> nodePrev = null;
            do {
                if(node.key.equals(key)) {
                    retValue = node.value;
                    if(nodePrev == null) { //first node in the list                        
                        block.table[index] = node.next;
                    } else {
                        nodePrev.next = node.next;
                    }
                }
                nodePrev = node;
                node = node.next;
            } while(node != null);            
            
        }
        
        block.lock.unlockWrite();        
//        refreshTable(); 
        ++modCount;
        return retValue;
    }
        
    private void refreshTable() {
        int fullSize = this.size();
        int hashTableSize = blockSize * blocksAmount;
        float loadFactorTemp = (float)hashTableSize / fullSize;
        if(loadFactorTemp < LOAD_FACTOR_DEFAULT) { 
            System.out.println("GlobalLockTry");
            if(globalLock.tryLock()) {
                System.out.println("GlobalLockOn");
                int newBlockSize = blockSize * BLOCK_SIZE_MULT;
                int newBlocksAmount = blocksAmount * BLOCKS_AMOUNT_MULT;                
                System.out.println(System.currentTimeMillis() + 
                                    ", size:" + fullSize + 
                                    ", LF:" + loadFactorTemp + 
                                    ", BlockSize:" + newBlockSize + 
                                    ", BlocksAmount:" + newBlocksAmount);

                ConcurHashMap newMap = 
                        new ConcurHashMap(newBlockSize, newBlocksAmount);
                for(Node<K,V> node:this.getAllAsList()) {
                    newMap.put(node.key, node.value);
                }
                
                this.blocks = newMap.blocks;
                this.blockSize = newMap.blockSize;
                this.blocksAmount = newMap.blocksAmount;

                globalLock.unlock();   
                System.out.println("GlobalLockOff");
            } else {
                System.out.println("Global lock busy");
            }
        }            
    }
    
    private int hash(Object key) {
        return (key == null) ? 0 : (key.hashCode() & 0x7FFFFFFF);
    }
    
    private BlockPtr getBlock(Object key) {
        int hash = hash(key);
        int globalTablelPtr = hash%(blocksAmount*blockSize);
        int blockNumber = globalTablelPtr/blockSize;
        int blockTablePtr = globalTablelPtr - (blockNumber * blockSize);         
        return new BlockPtr(blockNumber, blocks[blockNumber], blockTablePtr);
    }
    
    private class Block<K,V> {
        Node<K,V>[] table;
        RWLock lock;

        public Block(int blockSize, GlobalLock globalLock) {
            table = new Node[blockSize];
            lock = new RWLock(globalLock);
        }
           
    }
    
    private class BlockPtr {
        Block block;
        volatile int index;
        int blockId;
        
        public BlockPtr(int blockId, Block blockPtr, int indexPtr) {
            this.blockId = blockId;
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
