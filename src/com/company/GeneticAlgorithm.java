package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GeneticAlgorithm {

    public static void start(int generations, int startingPopulation){
        int[][] DNF = Input();
        Output(findDNF(DNF, generations,  startingPopulation));
    }

    public static int[][] Input(){
        File file = new File("input.txt");
        List<String> strings = new ArrayList<String>();
        Scanner sc;

        try {
            sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                strings.add(line);
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] stringsArray = strings.toArray(new String[0]);
        return StringArrayToIntMatrix(stringsArray);
    }
    public static void WriteGenerations(String output){
        try(FileWriter writer = new FileWriter("generations.txt", false))
        {
            writer.write(output);
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    public static void Output(String output){
        try(FileWriter writer = new FileWriter("output.txt", false))
        {
            writer.write(output);
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    public static int[][] StringArrayToIntMatrix(String[] str){
        int[][] matrix = new int[str.length][str[0].length()];
        for (int i = 0; i < str.length; i++){
            for (int j = 0; j < str[i].length(); j++){
                if (str[i].length() == str[0].length())
                    matrix[i][j] = str[i].charAt(j)-'0';
                else{
                    System.out.println("Неверный ввод!");
                    return null;
                }
            }
        }
        return matrix;
    }
    public static String CustomStringFromIntMatrix(int[][] array){
        String res = "";
        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[i].length; j++){
                res+=array[i][j];
                if (j< array[i].length-1)
                    res+=" ";
            }
            res+= "\n";
        }
        return res;
    }
    public static String CustomStringFromIntArray(int[] array){
        String res = "";
        for(int i = 0; i < array.length; i++){
            res+=array[i];
        }
        return res;
    }

    public static int[][] Creator(int variables){
        int[][] matrix = new int[(int)Math.pow(2, variables)][variables];
        for (int j = matrix[0].length-1; j >= 0; j--){
            for (int i = 0; i < matrix.length; i++){
                if (i % ((int)Math.pow(2, variables-j)) < (int)Math.pow(2, variables-j)/2)
                    matrix[i][j] = 0;
                else
                    matrix[i][j] = 1;
            }
        }

        return matrix;
    }



    public static String findDNF(int[][] DNF, int generations, int startingPopulation){
        String solution = "";
        int[][] masks = DNFtoMasks(DNF);
        for (int i = 0; i < DNF[0].length; i++){
            masks = DNFtoMasks(masks);
        }
        solution += "Intervals:\n" + CustomStringFromIntMatrix(masks) + "\n";

        int[][] intervalsTable = IntervalsTable(DNF, masks);
        solution +="\nTable of Intervals:\n" + CustomStringFromIntMatrix(intervalsTable) + "\n";

        ArrayList<Individual> individuals = new ArrayList<>();
        int[][] notUsedIntervals = new int[intervalsTable.length][intervalsTable[0].length];
        for (int i = 0; i < notUsedIntervals.length; i++){
            for (int j = 0; j < notUsedIntervals[i].length; j++){
                notUsedIntervals[i][j] = intervalsTable[i][j];
            }
        }
        for (int i = 0; i < startingPopulation; i++){
            individuals.add(CreateIndividual(intervalsTable, notUsedIntervals));
            //System.out.println("NUI:\n" + CustomStringFromIntMatrix(notUsedIntervals));
        }
        individuals = GeneticAlgorithm(individuals, generations, intervalsTable);
        /*for (int i = 0; i < individuals.size(); i++){
            //System.out.println("Individual №" + i + ":\n" + CustomStringFromIntMatrix(individuals.get(i).getGen()));
        }*/
        solution += "\nCovering \"" + CustomStringFromIntArray(individuals.get(0).getGenSummary()) + "\" is using intervals:\n";
        for (int i = 0; i < individuals.get(0).getGenSummary().length; i++){
            if (individuals.get(0).getGenSummary()[i] == 1){
                solution += CustomStringFromIntArray(masks[i]) + "\n";
            }
        }
        return solution;
    }




    public static int[][] DNFtoMasks(int[][] DNF){
        ArrayList<int[]> masks= new ArrayList<>();
        boolean[] usedInMask = new boolean[DNF.length];
        Arrays.fill(usedInMask, false);
        int difference;
        int toBeMasked = -1;
        for (int i = 0; i < DNF.length-1; i++){
            for (int j = 1; j < DNF.length; j++){
                difference = 0;
                for (int c = 0; c < DNF[j].length; c++){
                    if (DNF[i][c] != DNF[j][c]){
                        difference++;
                        if (difference == 1)
                            toBeMasked = c;
                        if (difference > 1){
                            break;
                        }
                    }
                }
                if (difference == 1){
                    int[] tmp = new int[DNF[0].length];
                    for (int c = 0; c < tmp.length; c++){
                        tmp[c] = DNF[i][c];
                    }
                    tmp[toBeMasked] = 2;
                    usedInMask[i] = true;
                    usedInMask[j] = true;
                    masks.add(Arrays.copyOf(tmp, tmp.length));
                    //System.out.println("Adding mask " + CustomStringFromIntArray(masks.get(i)) + "\ni=" + i + "\nj=" + j);
                }
            }
            if (!usedInMask[i]){
                masks.add(Arrays.copyOf(DNF[i], DNF[i].length));
            }
        }
        if (!usedInMask[DNF.length-1])
            masks.add(Arrays.copyOf(DNF[DNF.length-1], DNF[DNF.length-1].length));

        RemoveSame(masks);
        int[][] maskArray = new int[masks.size()][];
        for (int i = 0; i < maskArray.length; i++){
            maskArray[i] = masks.get(i);
        }
        return maskArray;
    }

    public static int[][] IntervalsTable(int[][] DNF, int[][] masks){
        int[][] intervalsTable = new int[DNF.length][masks.length];
        for (int i = 0; i < intervalsTable.length; i++){
            Arrays.fill(intervalsTable[i], 0);
        }
        boolean suits;
        for (int i = 0; i < masks.length; i++){
            for (int j = 0; j < DNF.length; j++){
                suits = true;
                for (int c = 0; c < DNF[i].length; c++){
                    if (DNF[j][c] != masks[i][c] && masks[i][c] != 2){
                        suits = false;
                        break;
                    }
                }
                if (suits){
                    intervalsTable[j][i] = 1;
                }
            }
        }
        return intervalsTable;
    }

    public static Individual CreateIndividual(int[][]table, int[][] notUsedIntervals){
        int sumNUL, sumTable;
        int[][] gens = new int[table.length][table[0].length];
        for (int i = 0; i < table.length; i++){
            sumNUL = 0;
            sumTable = 0;
            for (int j = 0; j < table[i].length; j++){
                if (notUsedIntervals[i][j] == 1){
                    sumNUL++;
                }
                if (table[i][j] == 1){
                    sumTable++;
                }
            }
            if (sumNUL == 0){
                int index = (int)(Math.random()*(sumTable-1))+1;
                sumTable = 0;
                for (int j = 0; j < table[i].length; j++){
                    if (table[i][j] == 1){
                        sumTable++;
                        if (index == sumTable){
                            gens[i][j] = 1;
                            break;
                        }
                    }
                }
            }
            else if (sumNUL == 1){
                for (int j = 0; j < notUsedIntervals[i].length; j++){
                    if (notUsedIntervals[i][j] == 1){
                        gens[i][j] = 1;
                        notUsedIntervals[i][j] = 0;
                        break;
                    }
                }
            }
            else{
                int index = (int)(Math.random()*(sumNUL))+1;
                if (index >= table[i].length)
                    while (true){
                        index = (int)(Math.random()*(sumNUL))+1;
                        if (index < table[i].length)
                            break;
                    }
                sumNUL = 0;
                for (int j = 0; j < table[i].length; j++){
                    if (notUsedIntervals[i][j] == 1){
                        sumNUL++;
                        if (index == sumNUL){
                            gens[i][j] = 1;
                            notUsedIntervals[i][j] = 0;
                            break;
                        }
                    }
                }
            }
        }
        return new Individual(Arrays.copyOf(gens, gens.length));
    }

    public static ArrayList<Individual> GeneticAlgorithm
            (ArrayList<Individual> individuals, int generations, int[][] table){
        int size;
        double selectionParameter = 1.55;
        int startingPopulation = individuals.size();
        System.out.println(startingPopulation);
        String outputLogs = "";
        for (int generation = 0; generation < generations; generation++){
            System.out.printf("Generation №%d, population:%d\n", generation, individuals.size());
            //System.out.println(individuals.size());
            outputLogs += individuals.size() + "\n";
            size = individuals.size();
            int random, notCrossedLeft, pair;
            boolean[] crossedOrMutated = new boolean[size];
            boolean[] forBreed = ForBreeding(individuals, 1);
            Arrays.fill(crossedOrMutated, false);
            for (int i = 0; i < size; i++){
                if (!crossedOrMutated[i]) {
                    if (forBreed[i]) {
                        random = (int) (Math.random() * 100);
                        if (random < 50) { //Crossing
                            //System.out.println("CROSSING!");
                            notCrossedLeft = 0;
                            for (int j = 0; j < size; j++) {
                                if (!crossedOrMutated[j]) {
                                    notCrossedLeft++;
                                }
                            }
                            if (notCrossedLeft <= 1) {
                                random = 50;
                            } else {
                                pair = i;
                                int alertCounter = 0;
                                while (pair == i) {
                                    int randomPair = (int) (Math.random() * notCrossedLeft);
                                    notCrossedLeft = 0;
                                    for (int j = 0; j < size; j++) {
                                        if (!crossedOrMutated[j] && j != i) {
                                            if (notCrossedLeft == randomPair) {
                                                pair = j;
                                                break;
                                            }
                                            notCrossedLeft++;
                                        }
                                    }
                                    alertCounter++;
                                    if (alertCounter > size * 10) {
                                        System.out.println("Loop in while!");
                                        System.out.printf("i = %d\nrandomPair = %d\nnotCrossedLeft = %d\n"
                                                , i, randomPair, notCrossedLeft);
                                        if (i != size - 1)
                                            pair = size - 1;
                                        else
                                            pair = size - 2;
                                        break;
                                    }
                                }

                                int cut = 1 + (int) (Math.random() * (individuals.get(i).getGen().length - 2));
                                int[][] tmp = new int[individuals.get(i).getGen().length][];
                                int[][] tmp2 = new int[individuals.get(i).getGen().length][];
                                for (int j = 0; j < cut; j++) {
                                    tmp[j] = Arrays.copyOf(individuals.get(i).getGen()[j],
                                            individuals.get(i).getGen()[j].length);
                                    tmp2[j] = Arrays.copyOf(individuals.get(pair).getGen()[j],
                                            individuals.get(pair).getGen()[j].length);
                                }
                                for (int j = cut; j < tmp.length; j++) {
                                    tmp2[j] = Arrays.copyOf(individuals.get(i).getGen()[j],
                                            individuals.get(i).getGen()[j].length);
                                    tmp[j] = Arrays.copyOf(individuals.get(pair).getGen()[j],
                                            individuals.get(pair).getGen()[j].length);
                                }

                                individuals.add(new Individual(tmp));
                                individuals.add(new Individual(tmp2));
                                crossedOrMutated[i] = true;
                                crossedOrMutated[pair] = true;
                            }
                        }
                        if (random >= 50) { //Mutating
                            //System.out.println("MUTATING!");
                            int mutatingGen = (int) (Math.random() * individuals.get(i).getGen().length);
                            int[][] gen = new int[individuals.get(i).getGen().length][];
                            for (int j = 0; j < gen.length; j++) {
                                gen[j] = Arrays.copyOf(individuals.get(i).getGen()[j],
                                        individuals.get(i).getGen()[j].length);
                            }
                            int availableMutations = 0;
                            int genToDelete = -1;
                            for (int j = 0; j < gen[mutatingGen].length; j++) {
                                if (gen[mutatingGen][j] == 1)
                                    genToDelete = j;
                                if (table[mutatingGen][j] == 1) {
                                    availableMutations++;
                                }
                            }
                            if (availableMutations > 1) {
                                int randomToMutate = (int) (Math.random() * availableMutations);
                                availableMutations = 1;
                                for (int j = 0; j < gen[mutatingGen].length; j++) {
                                    if (table[mutatingGen][j] == 1 && gen[mutatingGen][j] != 1) {
                                        if (availableMutations == randomToMutate) {
                                            gen[mutatingGen][j] = 1;
                                            gen[mutatingGen][genToDelete] = 0;
                                            individuals.add(new Individual(gen));
                                            crossedOrMutated[i] = true;
                                            break;
                                        }
                                        availableMutations++;
                                    }
                                }
                            }
                        }
                    }
                    else
                        crossedOrMutated[i] = true;
                }
            }
            if (individuals.size() > startingPopulation/4 && individuals.size() < startingPopulation*4)
                Selection(individuals, 1, selectionParameter);
            else if (individuals.size() >= startingPopulation * 4){
                Selection(individuals, 0, selectionParameter);
                selectionParameter = 1 + (selectionParameter - 1) / 2;
                System.out.println("Killing 3/4!");
            }
            else{
                selectionParameter = 2 - (2 - selectionParameter) / 2;
                System.out.println("No Selection!");
            }
            if (individuals.size() > size){
                if (selectionParameter > 1.01)
                    selectionParameter -= 0.01;
            }
            else{
                if (selectionParameter < 1.99)
                    selectionParameter += 0.01;
            }

        }
        Selection(individuals, 2, selectionParameter);
        WriteGenerations(outputLogs);
        return individuals;
    }
    public static boolean[] ForBreeding(ArrayList<Individual> individuals, int selectionType){
        boolean[] forBreed = new boolean[individuals.size()];
        Arrays.fill(forBreed, true);
        if (individuals.size() == 0){
            System.out.println("Size = 0\nselectionType =  " + selectionType);
            return forBreed;
        }
        int[] values = new int[individuals.size()];
        int[] order = new int[individuals.size()];
        for (int i = 0; i < values.length; i++){
            values[i] = individuals.get(i).getWeight();
        }
        int min;
        int minIndex;
        for (int i = 0; i < values.length; i++){
            values[i] = individuals.get(i).getWeight();
            order[i] = i;
        }
        for (int i = 0 ; i < values.length-1; i++){
            min = values[i];
            minIndex = i;
            for (int j = i+1; j < values.length; j++){
                if (values[j] < min){
                    min = values[j];
                    minIndex = j;
                }
            }
            if (minIndex != i){
                values[minIndex] = values[i];
                order[minIndex] = order[i];
                values[i] = min;
                order[i] = minIndex;
            }
        }

        switch (selectionType){
            case 0: //Based on weight
                for (int i = individuals.size()-1; i >= 0; i--){
                    if ((int)(Math.random()*individuals.get(i).getWeight()) > 1){
                        forBreed[i] = false;
                    }
                }
                break;
            case 1: //Linear
                for (int i = 0; i < individuals.size(); i++){
                    if ((int)(Math.random()*values.length) > values.length-i){
                        forBreed[order[i]] = false;
                    }
                }
                break;
            case 2: //Choosing Best
                for (int i = 0; i < individuals.size(); i++){
                    if (i != order[0])
                        forBreed[i] = false;
                }
                break;
        }
        return forBreed;
    }


    public static void Selection(ArrayList<Individual> individuals, int selectionType, double selectionParameter){
        if (individuals.size() == 0){
            System.out.println("Size = 0\nselectionType =  " + selectionType);
            return;
        }
        int[] values = new int[individuals.size()];
        int[] order = new int[individuals.size()];
        boolean[] forDelete = new boolean[individuals.size()];
        Arrays.fill(forDelete, false);
        for (int i = 0; i < values.length; i++){
            values[i] = individuals.get(i).getWeight();
        }
        int min;
        int minIndex;
        for (int i = 0; i < values.length; i++){
            values[i] = individuals.get(i).getWeight();
            order[i] = i;
        }
        for (int i = 0 ; i < values.length-1; i++){
            min = values[i];
            minIndex = i;
            for (int j = i+1; j < values.length; j++){
                if (values[j] < min){
                    min = values[j];
                    minIndex = j;
                }
            }
            if (minIndex != i){
                values[minIndex] = values[i];
                order[minIndex] = order[i];
                values[i] = min;
                order[i] = minIndex;
            }
        }

        switch (selectionType){
            case 0: //Killing 3/4
                for (int i = 0; i < individuals.size(); i++){
                    if (i%4 != 0){
                        forDelete[order[i]] = true;
                    }
                }
                break;
            case 1: //Linear
                for (int i = 0; i < individuals.size(); i++){
                    if ((int)(Math.random()*values.length) > ((values.length-i) * selectionParameter)){
                        forDelete[order[i]] = true;
                    }
                }
                break;
            case 2: //Choosing Best
                for (int i = 0; i < individuals.size(); i++){
                    if (i != order[0])
                        forDelete[i] = true;
                }
                break;
        }
        boolean genocide = true;
        int deletingNumber = 0;
        for (int i = 0; i < forDelete.length; i++){
            if (forDelete[i]){
                deletingNumber++;
            }
            else{
                genocide = false;
            }
        }
        if(genocide){
            if (selectionType == 2)
                System.out.println("Genociding last!!!");
            else{
                System.out.println("Genocide! Saving All!");
                return;
            }
        }
        /*if (selectionType != 2 && deletingNumber < (individuals.size()/4)){
            System.out.println("Saving Population!");
            Arrays.fill(forDelete, false);
        }*/
        for (int i = individuals.size()-1; i >=0; i--){
            if (forDelete[i]){
                individuals.remove(i);
            }
        }
    }

    public static void RemoveSame(ArrayList<int[]> ints){
        if (ints.size() < 2)
            return;
        /*for (int i = 0; i < ints.size(); i++){
            System.out.println("Before removing. Element №" + i + ": " + CustomStringFromIntArray(ints.get(i)));
        }*/
        for (int i = 0; i < ints.size()-1; i++){
            for (int j = 1; j < ints.size(); j++){
                if (j>i && Arrays.equals(ints.get(i), ints.get(j))){
                    /*System.out.printf("Removing element №%d: %s\nReason: equal to element №%d: %s\n",
                            j, CustomStringFromIntArray(ints.get(j)), i, CustomStringFromIntArray(ints.get(i)));*/
                    ints.remove(j);
                    j--;
                }
            }
        }
        /*for (int i = 0; i < ints.size(); i++){
            System.out.println("After removing. Element №" + i + ": " + CustomStringFromIntArray(ints.get(i)));
        }*/
    }

}
