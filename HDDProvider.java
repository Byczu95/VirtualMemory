/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teamone.operatingsystem.VirtualMemory2;

/**
 *
 * @author Łukasz Knop, Kaszuba Szymon
 */
public interface HDDProvider {

    void getPage(int programNumber, int pageNumber); //Dysk zapisuje stronnicę programu z dysku do pliku wymiany
}
