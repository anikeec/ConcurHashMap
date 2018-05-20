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
public class GlobalLock {
    
    private volatile boolean lockOn = false;
    private volatile int amountReadLocks = 0;
    private volatile int amountWriteLocks = 0;

    private boolean isLockOn() {
        return lockOn;
    }

    private void setLockOn(boolean lockOn) {
        this.lockOn = lockOn;
    }
    
    public void amountReadLocksInc() {
        synchronized(this) {
            if(amountReadLocks < Integer.MAX_VALUE)
                amountReadLocks++;
        }
    }
    
    public void amountReadLocksDec() {
        synchronized(this) {
            if(amountReadLocks > 0)
                amountReadLocks--;
            if((amountReadLocks == 0) && (amountWriteLocks == 0))
                this.notifyAll();
        }
    }
    
    public void amountWriteLocksInc() {
        synchronized(this) {
            if(amountWriteLocks < Integer.MAX_VALUE)
                amountWriteLocks++;
        }
    }
    
    public void amountWriteLocksDec() {
        synchronized(this) {
            if(amountWriteLocks > 0)
                amountWriteLocks--;
            if((amountReadLocks == 0) && (amountWriteLocks == 0))     
                this.notifyAll();
        }
    }
    
    public boolean tryLock() {
        boolean ret = false;
        synchronized(this) {
            if(!isLockOn()) {
                setLockOn(true);
                ret = true;
                while((amountReadLocks > 0) || (amountWriteLocks > 0)) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GlobalLock.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } 
        return ret;
    }
    
    public void unlock() {
        synchronized(this) {
            if(isLockOn())
                setLockOn(false); 
            this.notifyAll();
        }        
    }
    
    public boolean checkLockOn() {
        synchronized(this) {
            if(this.isLockOn()) 
                return true;
            return false;
        }        
    }
    
    public void waitLockFree() {
        synchronized(this) {
            while(this.isLockOn()) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConcurHashMap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
