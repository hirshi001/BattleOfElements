package com.hirshi001.game.util;

import java.util.Objects;

public class Pair<A, B> {

    public A a;
    public B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public Pair() {

    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public void setA(A a) {
        this.a = a;
    }

    public void setB(B b) {
        this.b = b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }

        Pair<?, ?> pair = (Pair<?, ?>) obj;
        return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
    }
}
