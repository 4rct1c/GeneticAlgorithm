package com.company;

import java.util.Arrays;

public class Individual {


    private int[][] gen;



    public Individual(){
        this.gen = null;
    }
    public Individual(int masks, int intervals){
        this.gen = new int[masks][intervals];
    }
    public Individual(int[][] gen){
        this.gen = gen;
    }
    public Individual(Individual otherIndividual){
        this.gen = otherIndividual.getGen();
    }

    public int[][] getGen(){
        return this.gen;
    }
    public void setGen(int[][] gen){
        this.gen = gen;
    }


    public int[] getGenSummary(){
        int[] sum = new int[gen[0].length];
        for (int i = 0; i < gen.length; i++){
            for (int j = 0; j < gen[i].length; j++){
                if (gen[i][j] == 1)
                    sum[j] = 1;
            }
        }
        return sum;

    }

    public boolean isCorrect(){
        int[] full = new int[this.getGenSummary().length];
        Arrays.fill(full, 1);
        return Arrays.equals(full, this.getGenSummary());
    }

    public int getWeight(){
        int weight = 0;
        for (int i = 0; i < this.getGenSummary().length; i++){
            weight+=this.getGenSummary()[i];
        }
        return weight;
    }

    public String toString(){
        String s = "";
        for (int i = 0; i < this.gen.length; i++){
            for (int j = 0; j < gen[i].length; j++){
                s+=this.getGen()[i][j];
            }
            s+="\n";
        }
        return s;
    }
}
