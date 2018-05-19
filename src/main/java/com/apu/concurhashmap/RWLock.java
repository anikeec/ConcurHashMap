/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.concurhashmap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author apu
 */
public class RWLock {
    
    private volatile int amountOfReadingThreads = 0;
    private volatile boolean writeLockOn = false;

    private boolean isWriteLockOn() {
        return writeLockOn;
    }

    private void setWriteLockOn(boolean writeLockOn) {
        this.writeLockOn = writeLockOn;
    }
    
    public void lockRead() {
        synchronized(this) {
            if(amountOfReadingThreads < Integer.MAX_VALUE)
                amountOfReadingThreads++;
        }
    }
    
    public void unlockRead() {
        synchronized(this) {
            if(amountOfReadingThreads > 0)
                amountOfReadingThreads--;
            if(amountOfReadingThreads == 0) {
                //we have to notify globalLock
            }                
        }
    }
    
    private boolean isReadLocked() {
        if(amountOfReadingThreads == 0)
            return true;               
        return false;
    }
    
    public void waitReadLockFree() {
        synchronized(this) {
            while(this.isReadLocked()) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConcurHashMap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void lockWrite() {
        synchronized(this) {
            if(!isWriteLockOn())
                setWriteLockOn(true);            
        }        
    }
    
    public void unlockWrite() {
        synchronized(this) {
            if(isWriteLockOn())
                setWriteLockOn(false);            
            this.notifyAll();
        }        
    }
    
    public boolean isWriteLocked() {
        return this.isWriteLockOn();
    }
    
    public void waitWriteLockFree() {
        synchronized(this) {
            while(this.isWriteLocked()) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConcurHashMap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
