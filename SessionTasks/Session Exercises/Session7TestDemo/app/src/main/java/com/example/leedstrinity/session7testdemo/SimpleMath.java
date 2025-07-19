package com.example.leedstrinity.session7testdemo;

public class SimpleMath {

    public int add(int op1, int op2) {
        return op1 + op2;
    }

    public int diff(int op1, int op2) {
        return op1-op2;
    }

    public double div(int op1, int op2) {
        if (op2 == 0) return 0;
        return op1 / op2;
    }
}
