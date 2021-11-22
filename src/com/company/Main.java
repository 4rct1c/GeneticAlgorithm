package com.company;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter number of generations: ");
        int generations = in.nextInt();
        System.out.println("Enter starting population: ");
        int population = in.nextInt();
        GeneticAlgorithm.start(generations, population);
    }
}
