/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teamone.operatingsystem.VirtualMemory2;

import java.util.LinkedList;

/**
 *
 * @author Łukasz Knop
 */
public class LRUStack {

    LRU node;
    public LinkedList<LRU> LRU_stack;         // stos LRU   

    LRUStack() {
        LRU_stack = new LinkedList<>();

        //System.out.println("[Pamięć Wirtualna]\tUtworzono stos LRU");
    }

    public void push(int PID, int pageNr, int frameNr) {
        node = new LRU(PID, pageNr, frameNr);
        LRU_stack.addFirst(node);
        //System.out.println("[Pamięć Wirtualna]\tNa stos LRU dodano " + frameNr + " ramkę, tj." + pageNr + " stronnicę procesu o ID: " + PID);
    }

    public void removeLast() {
        //System.out.println("[Pamięć Wirtualna]\tZe stosu LRU usunięto " + getLastElement().getFrameNumber() + " ramkę procesu ID: " + getLastElement().getProcessID());
        LRU_stack.removeLast();
    }

    public void moveTop(int PID, int pageNr, int frameNr) {

        LRU temp = new LRU(PID, pageNr, frameNr);
        for (int i = 0; i < LRU_stack.size(); i++) {
            if (LRU_stack.get(i).getFrameNumber() == frameNr) {
                LRU_stack.remove(LRU_stack.get(i));
                break;
            }
        }
        LRU_stack.addFirst(temp);
        //System.out.println("[Pamięć Wirtualna]\tNa szczyt stosu LRU przesunięto " + frameNr + " ramkę tj. " + pageNr + " stronnicę procesu o ID: " + PID);
    }

    public LRU getLastElement() {
        return LRU_stack.getLast();
    }

    public int getProccesID(int last) {
        return LRU_stack.getLast().getProcessID();
    }
}
