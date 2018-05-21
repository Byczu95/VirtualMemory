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
public class LRU {   //pojedynczy wezel stosu LRU

    private Page page;
    private int processID;
    private int frameNumber;

    LRU(int PID, int PageNr, int FrameNR) {
        processID = PID;
        frameNumber = FrameNR;
        page = new Page(PageNr, FrameNR);
    }

    public int getProcessID() {
        return processID;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public int getPageNumber() {
        return page.getPageNumber();
    }

}
