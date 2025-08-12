/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pietsgrafiekentestextended;

import java.awt.Color;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 *
 * @author Piet
 */
public class PietsGrafiekenTestExtended {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int nrOfPoints = 600;
        var f = new FunctionNameColorYfX(d -> d, "y = x", Color.red, -2, 2);
        var g = new FunctionNameColorRfPhi(d -> d / 4 * cos(.5 * d), "r = fi/4 * cos(fi/2)", Color.BLUE, 0, 4 * PI);
        var fs = PietsGrafieken.FourierSeries.getInstance()
                .withConstant(1)
                .withSinTerm((x, n) -> n % 2 == 0 ? 0d : sin(n * x) * -2 / (n * PI));
        ;
        var h = new FunctionNameColorYfX(d -> fs.apply(d, 10), "fourier with 10 terms", Color.black, -3, 3);
        new PietsGrafieken.Builder()
            .withPanelwidth(nrOfPoints)
            .withTitle("Piets Grafieken - test")
            .withXmin(-3)
            .withyMax(3)
            .withXmax(3)
            .withyMin(-3)
            .withPietsFunction(f)
            .withPietsFunction(g)
            .withPietsFunction(h)
            .build()
            .setVisible(true)
        ;
    }
    
}
