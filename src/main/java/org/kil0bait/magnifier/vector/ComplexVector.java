package org.kil0bait.magnifier.vector;

import org.kil0bait.magnifier.base.ComplexNumber;

public class ComplexVector {
    protected final ComplexNumber n1;
    protected final ComplexNumber n2;

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

    public ComplexVector sumHere(ComplexVector that) {
        this.n1.sumHere(that.n1);
        this.n2.sumHere(that.n2);
        return this;
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
}
