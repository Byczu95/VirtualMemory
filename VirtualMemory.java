/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teamone.operatingsystem.VirtualMemory2;

import com.teamone.operatingsystem.processmanagement.Process;

import com.teamone.operatingsystem.interpreter.MemoryException;
import java.io.File;

import com.teamone.operatingsystem.virtualmemory.SwapFileLastPage;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Łukasz Knop
 */
public class VirtualMemory implements
        com.teamone.operatingsystem.filesmanagement.VMProvider,
        com.teamone.operatingsystem.processmanagement.MemoryProvider,
        com.teamone.operatingsystem.interpreter.VMProvider {
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /* Zmienne */
    private final PhysicalMemory Memory;          ///pamięć fizyczna
    private final LRUStack lruStack;              //stos LRU
    public List<PageTable> processList;     //lista procesów, a dokładnie lista tablic stronnic
    private final List<Integer> freeFrames;       //lista wolnych ramek  
    HDDProvider Disk;                       //referencja do dysku
    private final SwapFileLastPage lastPage;      //zapamiętuje co znajduje się obecnie w pliku wymiany
    private static final String swapFile_dir = "src\\main\\java\\com\\teamone\\operatingsystem\\VirtualMemory2\\swapFile.txt";  // ścieżka pliku wymiany
    File swapFile = null;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Konstruktor */
    public VirtualMemory() throws IOException {
        //System.out.println("[Pamięć Wirtualna]\tUtworzono Pamięć Wirtualną");
        Memory = new PhysicalMemory();
        lruStack = new LRUStack();
        freeFrames = new ArrayList<>(16);
        processList = new ArrayList<>();

        swapFile = new File(swapFile_dir);
        swapFile.createNewFile();
        lastPage = new SwapFileLastPage();
        updateFreeFramesList();          //na starcie dodaję wszystkie ramki do listy wolnych ramek
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Ustawia referencję do dysku */
    public void setReferences(HDDProvider HardDisc) {
        this.Disk = HardDisc;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Funkcja czytająca z pliku wymiany */
    private String ReadSwapFile() throws IOException {
        String Code = new String();
        Scanner plik = new Scanner(swapFile);
        
        while (plik.hasNext())//Dopuki cos jest w pliku bd czytało, a bd w niej jedna stronnica
        {        
            Code += plik.nextLine();
        }
        plik.close();
        //System.out.println("[Pamięć Wirtualna]\tNastapil odczyt z pliku wymiany");
        return Code;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Metoda odświeżająca wolne ramki na starcie systemu */
    private void updateFreeFramesList() {
        for (int i = 0; i < 16; i++) {
            isFree(i);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Funkcja sprawdzająca czy dana ramka jest wolna */
    private boolean isFree(int frameNr) {
        //System.out.println("[Pamięć Wirtualna]\tSprawdzam czy ramka " + frameNr + " jest wolna");
        if (Memory.memory[frameNr][0] == '\0') {
            if (freeFrames.contains(frameNr) != true) {
                freeFrames.add(frameNr);         //dodawanie na liste wolnych ramek
            }
            return true;
        } else {
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Funkcja obliczająca, na której stronnicy znajduję się potrzebny kod programu */
    private int whichPage(int adress) {
        if (adress < 32) {
            return 0;
        }
        return adress / 32;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Funkcja obliczająca, na której pozycji w ramce znajduję się potrzebny kod programu */
    private int whichColumn(int adress) //przeliczenie odległośc od początku wiersza(ramki) dla podanego adresu 
    {
        while (adress >= 32) {

            adress -= 32;
            if (adress < 32) {
                return adress % 32;
            }
        }
        return adress % 32;

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Metoda wyszukująca i usuwająca ramkę ofiarę */
    private void removeVictim() {
        boolean removed = true;
        int pageNr = lruStack.getLastElement().getPageNumber();
        int victim = lruStack.getLastElement().getFrameNumber();
         
        for (int i = 0; i < processList.size(); i++) {
            if (processList.get(i).processID == lruStack.getLastElement().getProcessID()) {  //jeżeli proces się nie zakończył
                if (processList.get(i).pageTable[pageNr].getFrameNumber() == victim && processList.get(i).pageTable[pageNr].isValid()) {
                    processList.get(i).pageTable[pageNr].deleteFrame();   //w tablicy stronnic dla tej stronnicy ustawiam wskazywana ramke na -1, a bit na invalid
                    lruStack.removeLast();    //usuwam ramke ze stosu
                    removed = false;
                    break;
                }
            }
        }
        if (removed) {
            lruStack.removeLast();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Metoda usuwająca ramkę z listy wolnych ramek */
    private void removeFreeFrame(Integer element) {
        if (freeFrames.contains(element)) {
            freeFrames.remove(element);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Funkcja zwracająca ścieżkę do pliku wymiany */
    @Override
    public String getPath() {   //Zwraca  ścieżkę do pliku wymiany
        return swapFile_dir;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Tworzenie tablicy stronnic dla procesu oraz sprowadzanie jego zerowej stronnicy do pamięci */
    @Override 
    public void provideMemoryForProcess(Process PCB, int sizeOfProgramm) throws  IOException {  //tylko sprowadzam 1 stronnice i wprowadzam do pamieci
        
        PageTable pageTable = new PageTable(PCB.getProcessName(), PCB.getProgrammName(), sizeOfProgramm); //tworzę tablicę stronnic dla procesu

        if (lastPage.WhichPage != 0 || lastPage.WhichProgram != PCB.getProgrammName()) {
            Disk.getPage(PCB.getProgrammName(), 0);  //sprowadzana jest zerowa stronica procesu
            lastPage.WhichPage = 0;
            lastPage.WhichProgram = PCB.getProgrammName();
        }

        if (freeFrames.isEmpty()) {

            //System.out.println("[Pamięć Wirtualna]\tNie znaleziono wolnej ramki");
            int free = lruStack.getLastElement().getFrameNumber();
            removeVictim();
            Memory.writeToMemory(free, ReadSwapFile()); //zapisuję stronnice w pamięci
            pageTable.push(0, free);       //do zerowego indeksu tablicy stronic dodac numer odpowiadajac ramki w pamieci]
            lruStack.push(PCB.getProcessName(), 0, free);  //pobieram proces
        } else {
            Integer frame;
            for (int i = 0; i < freeFrames.size(); i++) {
                frame = freeFrames.get(i);
                //System.out.println("[Pamięć Wirtualna]\tZnaleziono wolną ramkę: " + i);
                Memory.writeToMemory(frame, ReadSwapFile()); ///zapisuję stronnice w pamięci
                pageTable.push(0, frame);       //do zerowego indeksu tablicy stronnic dodaję numer odpowiadajac ramki w pamieci
                lruStack.push(PCB.getProcessName(), 0, frame);  //
                removeFreeFrame(frame);
                break;
            }
        }
        processList.add(pageTable);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Usuwanie tablicy stronnic procesu */
    @Override
    public void freeMemoryFromProcess(Process PCB) {

        for (int i = 0; i < processList.size(); i++) {
            if (processList.get(i).processID == PCB.getProcessName()) {
                processList.remove(i);
                break;
            }
        }
        System.out.println("[Pamięć Wirtualna]\tUsunięto tablicę stronnic dla procesu");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Funkcja zwracająca potrzebny fragment kodu */
    @Override
    public String getCode(Process PCB, int address, int size) throws MemoryException, IOException {  
        //System.out.println("[Pamięć Wirtualna]\tSzukanie kodu w pamięci");
        String Code = "";

        int page = whichPage(address);
        char[] data = new char[size];

        int memoryIndex = whichColumn(address);
        boolean throwException = false;
        boolean goToNextFrame = true;

        
        for (int i = 0; i < processList.size(); i++) {
            if (processList.get(i).processID == PCB.getProcessName() && goToNextFrame) {
                if (processList.get(i).pageTable[page].isValid()) {     //stronnica jest w pamiéci

                    for (int j = 0; j < size; j++) {

                        Integer frame = processList.get(i).pageTable[page].getFrameNumber();
                        data[j] = Memory.readMemory(frame, memoryIndex);//czytam
                        Code += data[j];
                        lruStack.moveTop(PCB.getProcessName(), page, frame);
                        memoryIndex++;
                        removeFreeFrame(frame);

                        if (memoryIndex >= 32) {
                            memoryIndex = 0;
                            goToNextFrame = false;
                            break;
                        }
                    }
                    if (Code.length() != size) {
                        throwException = true;
                    } else {
                        break; //przerywam pętlę jeśli odczytam kod
                    }
                } else {  //stronnicy nie ma w pamiéci

                    if (lastPage.WhichPage != page || lastPage.WhichProgram != PCB.getProgrammName()) {
                        Disk.getPage(PCB.getProgrammName(), page);
                        lastPage.WhichPage = page;
                        lastPage.WhichProgram = PCB.getProgrammName();
                    }
                    if (freeFrames.isEmpty() && goToNextFrame) {  //nie ma wolnych ramek

                        //System.out.println("[Pamięć Wirtualna]\tNie znaleziono stronniy w pamięci. Nie znaleziono wolnych ramek");
                        int free = lruStack.getLastElement().getFrameNumber();
                        removeVictim(); //znajdz ofiare //usun
                        processList.get(i).push(page, free);     //dodawanie indeksu do tablicy stronnic tego procesu
                        Memory.writeToMemory(free, ReadSwapFile()); //zapisuję stronnice w pamięci
                        lruStack.push(PCB.getProcessName(), page, free); //dodaję ramkę na szczyt stosu LRU

                        throwException = true;
                        break;
                    } else {  //znaleziono wolná ramké
                        Integer frame;
                        for (int k = 0; k < freeFrames.size(); k++) {
                            frame = freeFrames.get(k);
                            if (isFree(frame) && goToNextFrame) {

                                //System.out.println("[Pamięć Wirtualna]\tNie znaleziono stronnicy w pamięci. Znaleziono walna ramke");

                                processList.get(i).push(page, frame);     //dodawanie indeksu do tablicy stronnic tego procesu
                                Memory.writeToMemory(frame, ReadSwapFile()); //zapisuję stronnice w pamięci
                                lruStack.push(PCB.getProcessName(), page, frame); //dodaję ramkę na szczyt stosu LRU
                                removeFreeFrame(frame);    //usuwam wolną ramkę z listy
                                throwException = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (Code.length() == size) {
                break; //przerywam pętlę jeśli odczytam kod
            }
        }
        //writeMemoryToConsole();
        //writeLRUtoConsole();
        //writeFreeFramesToConsole();
        //writePageTablesToConsole();
        if (throwException) {
            //System.out.println("[Pamięć Wirtualna]\tSprowadzono dane z pamięci. Rzucam wyjątek");
            throw new MemoryException(Code);
        } else {
            if (Code == null) {
                throw new NullPointerException("Proces o takim ID nie istnieje");
            }
            System.out.println("[Pamięć Wirtualna]\tSprowadzono dane z pamięci.");
            return Code;
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Wypisywanie zawartośći pamięci na konsolę */
    @Override
    public void writeMemoryToConsole() {

        System.out.println("[Pamięć Wirtualna][LRU ze stosem]\tZawartość Pamięci");
        for (int i = 0; i < 16; i++) {
            System.out.print(i+". \t");
            for (int j = 0; j < 32; j++) {
                System.out.print(Memory.memory[i][j]);
                if (j % 4 == 3) {
                    System.out.print('\t');
                }
            }
            System.out.println();
        }
        writeLRUtoConsole();
        writePageTablesToConsole();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void writeFreeFramesToConsole() {
        System.out.println("[Pamięć Wirtualna]\tWolne ramki");
        for (int i = 0; i < freeFrames.size(); i++) {
            System.out.println(freeFrames.get(i));
        }
    }

    public void writeLRUtoConsole() {
        System.out.println("[Pamięć Wirtualna]\tZawartość stosu LRU");
        for (int i = 0; i < lruStack.LRU_stack.size(); i++) {
            System.out.println("\t\tRamka: "+lruStack.LRU_stack.get(i).getFrameNumber());  //numery ramek
        }
    }

    public void writePageTablesToConsole() {
        for (int i = 0; i < processList.size(); i++) {
            System.out.println("Proces: " + processList.get(i).processID);
            for (int j = 0; j < processList.get(i).pageTableSize; j++) {
                System.out.println("\t\tStronnica: " + processList.get(i).pageTable[j].getPageNumber() + "\tRamka: " 
                        + processList.get(i).pageTable[j].getFrameNumber()
                        + "\tBit: " + processList.get(i).pageTable[j].isValid() );

            }
            System.out.println();
        }
    }
}
