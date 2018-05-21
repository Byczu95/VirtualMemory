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
public class PhysicalMemory {

    protected char[][] memory;

    PhysicalMemory() {

        memory = new char[16][32];

    }

    public void writeToMemory(int frameNr, String data)//zapisuje bajt do podanego adresu pamięci 
    {

        char[] Data = new char[data.length()];
        Data = data.toCharArray();
        for (int i = 0; i < Data.length; i++) {
            //System.out.println("[Pamięć Wirtualna]\tZapisano Bajt danych do pamięci");
            memory[frameNr][i] = Data[i];
        }
    }

    public char readMemory(int frameNr, int offset){ //odczytuje bajt z podanego adresu pamięci 
        //System.out.println("[Pamięć Wirtualna]\tOdczytano Bajt danych z pamięci");
        return memory[frameNr][offset];//odczytuje bajt z pamięći 
    }

}
