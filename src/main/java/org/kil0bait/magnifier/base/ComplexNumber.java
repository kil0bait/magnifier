package org.kil0bait.magnifier.base;

import java.util.Objects;

public class ComplexNumber {
    protected double re;
    protected double im;

    public ComplexNumber(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public ComplexNumber(ComplexNumber that) {
        this.re = that.re;
        this.im = that.im;
    }

    private ComplexNumber() {
    }

    public double norm() {
        return re * re + im * im;
    }

    public double length() {
        return Math.sqrt(norm());
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(re, -im);
    }

    public ComplexNumber sum(ComplexNumber that) {
        ComplexNumber res = new ComplexNumber();
        res.re = this.re + that.re;
        res.im = this.im + that.im;
        return res;
    }

    public ComplexNumber sumHere(ComplexNumber that) {
        this.re += that.re;
        this.im += that.im;
        return this;
    }

    public ComplexNumber subHere(ComplexNumber that) {
        this.re -= that.re;
        this.im -= that.im;
        return this;
    }

    public ComplexNumber mul(ComplexNumber that) {
        ComplexNumber res = new ComplexNumber();
        res.re = this.re * that.re - this.im * that.im;
        res.im = this.re * that.im + this.im * that.re;
        return res;
    }

    public ComplexNumber div(ComplexNumber that) {
        return this.mul(that.conjugate()).divByNumber(that.norm());
    }

    public ComplexNumber divByNumber(double div) {
        return new ComplexNumber(re / div, im / div);
    }

    public ComplexNumber divByNumberHere(double div) {
        this.re /= div;
        this.im /= div;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexNumber that = (ComplexNumber) o;
        return re == that.re && im == that.im;
    }

    public int hashCode() {
        return Objects.hash(re, im);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        if (re < 0)
            s.append("-").append(numberFormat(-re));
        else if (re > 0)
            s.append(numberFormat(re));
        if (im < 0)
            s.append("-").append(im != -1 ? numberFormat(-im) : "").append("i");
        else if (im == 1)
            s.append(re != 0 ? "+" : "").append("i");
        else if (im > 0)
            s.append(re != 0 ? "+" : "").append(numberFormat(im)).append("i");
        else if (re == 0)
            s.append("0");
        return s.toString();
    }

    public static ComplexNumber zero() {
        return new ComplexNumber(0, 0);
    }

    public static String numberFormat(double d) {
        return String.valueOf(d);
    }

    public static ComplexNumber parseComplex(String stringElem) {
        String s = stringElem;
        if (!s.contains("i")) {
            s += "+0i";
        } else if (s.length() == 1) {
            s = "0+1i";
        } else {
            s = s.replaceFirst("-i", "-1i")
                    .replaceFirst("\\+i", "+1i")
                    .replace("-", "+-");
            if (s.charAt(0) == '+')
                s = s.replaceFirst("\\+", "");
            if (!s.contains("+"))
                s = "0+" + s;
        }
        return finalParse(s.replaceFirst("i", ""));
    }

    private static ComplexNumber finalParse(String s) {
        String[] temp = s.split("\\+");
        return new ComplexNumber(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
    }

}
