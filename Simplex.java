class Simplex {
  Fraction[] base;
  int[] index;
  Fraction[] autres_variables;
  Fraction[][] tableau;
  int width;
  boolean show;
  int height;
  boolean isDelta = false;
  
  Simplex(int nb_base_, int nb_contrainte_, Fraction[][] coef_, Fraction[] limiteurs_, boolean show_) {
    if (check2phases(limiteurs_)) {
        Simplex delta;
        Fraction[][] coef_delta = new Fraction[coef_.length+1][coef_[0].length];
        for (int i = 0; i < coef_delta.length - 1; i++) {
            for (int j = 0; j < coef_delta[0].length; j++) {
                if (j != 0) {
                    coef_delta[i][j] = coef_[i][j].copy();
                } else {
                    coef_delta[i][j] = new Fraction(0);
                }
                coef_delta[coef_delta.length - 1][j] = new Fraction(-1);
            }
        }
        delta = new Simplex(nb_base_+1, nb_contrainte_, coef_delta, limiteurs_);
        delta.solve();
        if (delta.base[delta.base.length - 1].value() > 0) {
            System.out.println("L'ensemble est vide");
            return;
        }
        width = nb_base_ + nb_contrainte_+1;
        height = nb_contrainte_+1;
        tableau = new Fraction[width][height];
        for (int i = 0; i < tableau.length; i++) {
            for (int j = 0; j < tableau[0].length; j++) {
                if (i < delta.index[delta.base.length]) {
                    tableau[i][j] = delta.tableau[i][j].copy();
                }
                if (i > delta.index[delta.base.length]) {
                    tableau[i][j] = delta.tableau[i-1][j].copy();
                }
            }
        }

        for (int i = 0; i < nb_base_; i++) {
            if (delta.index[i] > delta.base.length) {
                combiLin(0, delta.index[i] - delta.base.length, coef_[i][0]);
            } else {
                tableau[0][i] = Fraction.add(tableau[0][i], coef_[i][0]); 
            }
        }
    }
    
    show = show_;
    base = new Fraction[nb_base_];
    index = new int[nb_base_ + nb_contrainte_];
    for (int i = nb_base_; i < index.length; i++) {
        index[i] = i - nb_base_ + 1;
    }
    autres_variables = new Fraction[nb_contrainte_];
    width = nb_base_ + nb_contrainte_+1;
    height = nb_contrainte_+1;
    tableau = new Fraction[width][height];
    for (int i = 0; i < coef_.length; i++) {
        for (int j = 0; j < coef_[0].length; j++) {
            tableau[i][j] = coef_[i][j].copy();
        }
    }
    for (int i = coef_.length; i < width-1; i++) {
        for (int j = 0; j < height; j++) {
            if (j-1 == i - coef_.length) {
                tableau[i][j] = new Fraction(1);
            } else {
                tableau[i][j] = new Fraction(0);
            }
        }
    }
    tableau[width-1][0] = new Fraction(0);
    for (int j = 1; j < height; j++) {
        tableau[width-1][j] = limiteurs_[j-1].copy();
    }
  }

  Simplex(int nb_base_, int nb_contrainte_, Fraction[][] coef_, Fraction[] limiteurs_) {    
    isDelta = true;
    show = true;
    base = new Fraction[nb_base_];
    index = new int[nb_base_ + nb_contrainte_];
    for (int i = nb_base_; i < index.length; i++) {
        index[i] = i - nb_base_ + 1;
    }
    autres_variables = new Fraction[nb_contrainte_];
    width = nb_base_ + nb_contrainte_+1;
    height = nb_contrainte_+1;
    tableau = new Fraction[width][height];
    for (int i = 0; i < coef_.length; i++) {
        for (int j = 0; j < coef_[0].length; j++) {
            tableau[i][j] = coef_[i][j].copy();
        }
    }
    for (int i = coef_.length; i < width-1; i++) {
        for (int j = 0; j < height; j++) {
            if (j-1 == i - coef_.length) {
                tableau[i][j] = new Fraction(1);
            } else {
                tableau[i][j] = new Fraction(0);
            }
        }
    }
    tableau[width-1][0] = new Fraction(0);
    for (int j = 1; j < height; j++) {
        tableau[width-1][j] = limiteurs_[j-1].copy();
    }
  }

  int entrante_index() {
    if (isDelta && index[base.length-1] >= base.length) {
        return index[base.length-1] - base.length;
    }
    int index = 0;
    for (int i = 1; i < width; i++) {
        if (tableau[i][0].value() > tableau[index][0].value()) {
            index = i;
        }
    }
    return index;
  }

  int sortante_index(int entrante_index) {
    Fraction min = new Fraction(-1);
    int index_min = -1;
    Fraction value;
    for (int j = 1; j < height; j++) {
        if (tableau[entrante_index][j].value() > 0) {
            value = Fraction.mult(tableau[width-1][j], tableau[entrante_index][j].copy().inverse());
            if (min.value() == -1 || value.value() < min.value()) {
                min = value.copy();
                index_min = j;
            }
        }
    }
    
    return index_min;
  }

  Fraction solve() throws ArithmeticException{
    int entrante_index = entrante_index();
    if (show) {
        printTab(0, 0);
    }
    while (tableau[entrante_index][0].value() > 0) {
        
        int sortante_index = sortante_index(entrante_index);
        if (sortante_index == -1) {
            throw new ArithmeticException("L'ensemble est non borné");
        }

        pivot(entrante_index, sortante_index);
        update_base(entrante_index, sortante_index);
        if (show) {
            printTab(entrante_index, sortante_index);
        }
        entrante_index = entrante_index();
    }
    for (int i = 0; i < base.length; i++) {
        if (index[i] == 0) {
            base[i] = new Fraction(0);
        } else {
            base[i] = tableau[width - 1][index[i]].copy();
        }
    }
    return Fraction.mult(tableau[width-1][0], new Fraction(-1));
  }

  void pivot(int entrante, int sortante) {
    Fraction coef = tableau[entrante][sortante].copy().inverse();
    for (int i = 0; i < width; i++) {
        tableau[i][sortante] = Fraction.mult(tableau[i][sortante], coef);
    }
    for (int j = 0; j < height; j++) {
        if (j != sortante) {
            combiLin(j, sortante, Fraction.mult(tableau[entrante][j], new Fraction(-1)));
        }
    }
  }

  void combiLin(int x, int y, Fraction alpha) {
    for (int i = 0; i < width; i++) {
        tableau[i][x] = Fraction.add(tableau[i][x], Fraction.mult(alpha, tableau[i][y]));
    }
  }

  void update_base(int entrante, int sortante) {
    int temp = index[entrante];
    index[entrante] = index[sortante + base.length - 1];
    index[sortante + base.length - 1] = temp;
  }
  
  void printTab(int entrante, int sortante) {
    System.out.println("La variable entrante est la " + entrante + "ieme variable");
    System.out.println("La variable sortante est la " + sortante + "ieme variable");
    for (int j = 0; j < height; j++) {
        String line = "";
        for (int i = 0; i < width; i++) {
            line += tableau[i][j];
            line += " ";
        }
        System.out.println(line);
    }
    System.out.println();
  }

  boolean check2phases(Fraction[] limiteur) {
    for (int i = 0; i < limiteur.length; i++) {
        if (limiteur[i].value() < 0) {
            return true;
        }
    }
    return false;
  }
}