package net.rushnation.rushyprox.config;

import java.util.Arrays;

public class TestObject {

    public String testString = "Das ist ein Test";

    public String[] testArray = {"Das", "Ist", "Ein", "Test"};

    public char c = 's';

    @Override
    public String toString() {
        return "TestObject{" +
                "testString='" + testString + '\'' +
                ", testArray=" + Arrays.toString(testArray) +
                ", c=" + c +
                '}';
    }
}
