public class Fraction{
    public int numerator;
    public int denominator;
    
    public Fraction(int numerator_, int denominator_) throws ArithmeticException {
        numerator = numerator_;
        if (denominator_ == 0) {
            throw new ArithmeticException("Division by 0");
        }
        denominator = denominator_;
        simplify();
    }

    public Fraction(int value_) {
        numerator = value_;
        denominator = 1;
    }

    public static Fraction add(Fraction f1, Fraction f2) {
        int new_numerator = f1.numerator * f2.denominator + f2.numerator * f1.denominator;
        int new_denominator = f1.denominator * f2.denominator;
        return new Fraction(new_numerator, new_denominator).simplify();
    }

    public static Fraction mult(Fraction f1, Fraction f2) {
        Fraction res =  new Fraction(f1.numerator * f2.numerator, f1.denominator * f2.denominator);
        res.simplify();
        return res;
    }

    public Fraction simplify() {
        boolean negative = (numerator * denominator) < 0;
        numerator = Math.abs(numerator);
        denominator = Math.abs(denominator);
        if (numerator == 0) {
            denominator = 1;
            return this;
        }
        int[] eratostene = new int[Math.max(numerator, denominator)];
        for (int i = 0; i < eratostene.length; i++) {
            eratostene[i] = i%2;
        }
        if (eratostene.length > 1) {
            eratostene[1] = 0;
        }
        if (eratostene.length > 2) {
            eratostene[2] = 1;
        }

        for (int i = 3; i < Math.sqrt(eratostene.length); i += 2) {
            if (eratostene[i] == 1) {
                int count = 2*i;
                while (count < eratostene.length) {
                    eratostene[count] = 0;
                    count += i;
                }
            }
        }
        for (int i = 2; i < eratostene.length; i++) {
            if (eratostene[i] == 1) {
                while (numerator % i == 0 && denominator % i == 0) {
                    numerator /= i;
                    denominator /= i;
                }
            }
        }
        if (negative) {
            numerator *= -1;
        }
        return this;
    }

    @Override
    public String toString() {
        simplify();
        if (denominator != 1) 
            return "" + numerator + "/" + denominator;
        return "" + numerator;
    }

    @Override
    public boolean equals(Object f) {
        if (f instanceof Fraction) {
            return numerator == ((Fraction)f).numerator && denominator == ((Fraction)f).denominator;
        } else if (f instanceof Integer) {
            return numerator == (int)f && denominator == 1;
        }
        return false;
    }

    public float value() {
        return (float)numerator/denominator;
    }

    public Fraction inverse() {
        int temp = numerator;
        numerator = denominator;
        denominator = temp;
        return this;
    }

    public Fraction copy() {
        return new Fraction(numerator, denominator);
    }
}