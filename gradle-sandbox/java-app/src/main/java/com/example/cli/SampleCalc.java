package com.example.cli;

/**
 * Hello.
 * Created by kyokomi on 15/01/16.
 */
public class SampleCalc {

    public static void main(String[] args) {
        System.out.println("Hello ");

        for (String arg : args) {
            System.out.println(arg);
        }
    }
}
