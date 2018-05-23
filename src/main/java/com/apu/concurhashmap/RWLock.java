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
    private final GlobalLock globalLock;

    public RWLock(GlobalLock globalLock) {
        this.globalLock = globalLock;
    }

    private boolean isWriteLockOn() {
        return writeLockOn;
    }

    private void setWriteLockOn(boolean writeLockOn) {
        this.writeLockOn = writeLockOn;
    }
    
    public void lockRead() {        
        synchronized(this) {
            waitWriteLockFree();
            if(amountOfReadingThreads < Integer.MAX_VALUE)
                amountOfReadingThreads++;
        }
        globalLock.amountReadLocksInc();
    }
    
    public void unlockRead() {
        synchronized(this) {
            if(amountOfReadingThreads > 0)
                amountOfReadingThreads--;
            if(amountOfReadingThreads == 0) {
                //we have to notify globalLock
                this.notifyAll();
            }                
        }
        globalLock.amountReadLocksDec();
    }
    
    private boolean isReadLocked() {
        if(amountOfReadingThreads == 0)
            return false;
        return true;
    }
    
    private void waitReadLockFree() {
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
            waitWriteLockFree();
            waitReadLockFree();
            if(!isWriteLockOn()) {
                setWriteLockOn(true);            
//                System.out.println(Thread.currentThread().getName() + " - wLon");
            }
        }   
        globalLock.amountWriteLocksInc();
    }
    
    public void unlockWrite() {
        synchronized(this) {
            if(isWriteLockOn()) {
                setWriteLockOn(false); 
//                System.out.println(Thread.currentThread().getName() + " - wLoff");
            }
        }
        globalLock.amountWriteLocksDec();
        synchronized(this) {
//            System.out.println(Thread.currentThread().getName() + " - wLn");
            this.notifyAll();
        }        
    }
    
    public boolean isWriteLocked() {
        return this.isWriteLockOn();
    }
    
    private void waitWriteLockFree() {
        synchronized(this) {
//            System.out.println(Thread.currentThread().getName() + " - wLf");
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
