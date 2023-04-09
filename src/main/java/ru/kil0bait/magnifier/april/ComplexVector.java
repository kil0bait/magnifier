package ru.kil0bait.magnifier.april;

import ru.kil0bait.magnifier.classes.ComplexNumber;

public class ComplexVector {
    private final ComplexNumber n1;
    private final ComplexNumber n2;

    public ComplexVector(ComplexNumber n1, ComplexNumber n2) {
        this.n1 = new ComplexNumber(n1);
        this.n2 = new ComplexNumber(n2);
    }

    public ComplexVector(double f1, double f2) {
        this.n1 = new ComplexNumber(f1, 0);
        this.n2 = new ComplexNumber(f2, 0);
    }

    public ComplexVector mul(ComplexNumber c) {
        return new ComplexVector(
                this.n1.mul(c),
                this.n2.mul(c)
        );
    }

    public ComplexVector sum(ComplexVector that) {
        return new ComplexVector(
                this.n1.sum(that.n1),
                this.n2.sum(that.n2)
        );
    }

    public ComplexVector sumHere(ComplexVector that) {
        this.n1.sumHere(that.n1);
        this.n2.sumHere(that.n2);
        return this;
    }

    public ComplexVector sub(ComplexVector that) {
        return new ComplexVector(
                this.n1.sub(that.n1),
                this.n2.sub(that.n2)
        );
    }

    public ComplexVector subHere(ComplexVector that) {
        this.n1.subHere(that.n1);
        this.n2.subHere(that.n2);
        return this;
    }

    public ComplexNumber scalarMul(ComplexVector that) {
        return this.n1.mul(that.n1.conjugate())
                .sum(this.n2.mul(that.n2.conjugate()));
    }

    public static ComplexVector zero() {
        return new ComplexVector(0, 0);
    }

    public void divByNumberHere(double div) {
        this.n1.divByNumberHere(div);
        this.n2.divByNumberHere(div);
    }

    public void mulByNumberHere(double mul) {
        this.n1.mulByNumberHere(mul);
        this.n2.mulByNumberHere(mul);
    }

    public ComplexNumber getN1() {
        return n1;
    }

    public ComplexNumber getN2() {
        return n2;
    }
}
