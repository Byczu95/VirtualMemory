/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teamone.operatingsystem.VirtualMemory2;

/**
 *
 * @author Łukasz Knop
 */
public class PageTable {
    
    int processID;
    int programmID;            
    public Page[] pageTable;  // tablica stronnic dla procesu
    int pageTableSize;

    PageTable(int processNr, int programmNr, int sizeOfProgramm) {    //jako argument przekazac dysk?

        programmID = programmNr;
        processID = processNr;
        pageTableSize = sizeOfProgramm;
        pageTable = new Page[pageTableSize];
 
        for (int i = 0; i < pageTableSize; i++) {
            pageTable[i] = new Page();
            pageTable[i].setPage(i);
        }
        System.out.println("[Pamięć Wirtualna]\tUtworzono tablicę stronnic dla procesu: " + processNr + " o rozmiarze: " + pageTableSize);
    }

    public void push(int pageNr, int frameNr) {
        pageTable[pageNr].setPage(pageNr);
        pageTable[pageNr].setFrame(frameNr);
    }

    public void pop(int pageNr, int frameNr) {
        pageTable[pageNr].deleteFrame();
    }

};
