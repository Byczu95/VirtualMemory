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
//pojedynczy wezel tablicy stronnic
public class Page //przechowuje sposób odwzorowania pamięci logicznej programu na adres fizyczny
{

    private int pageNumber;
    private boolean valid;
    private int frameNumber;

    Page() {
        pageNumber = -1;
        frameNumber = -1;
        valid = false;
    }

    Page(int pageNr, int frameNr) {
        pageNumber = pageNr;
        frameNumber = frameNr;
        valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setFrame(int FrameNr) {
        frameNumber = FrameNr;
        valid = true;
    }

    public void setPage(int page) {
        pageNumber = page;
    }

    public void deleteFrame() {  //ramka usuwana jest z pamieci
        frameNumber = -1;
        valid = false;
    }
}
