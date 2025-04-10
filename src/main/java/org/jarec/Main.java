package org.jarec;

import org.jarec.util.PropertyHandler;

public class Main {
    public static void main(String[] args) {
        PropertyHandler.load("splurg.properties");

        System.out.println(PropertyHandler.get("splurg.author", "Big Ted"));
    }
}