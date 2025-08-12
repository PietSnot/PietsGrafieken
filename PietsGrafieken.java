/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pietsgrafiekentestextended;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import static java.lang.Double.min;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Piet
 */
public class PietsGrafieken {

    /**
     * @param args the command line arguments
     */
    
    //==========================================================================
    //  static methods
    //==========================================================================
    static Point2D p2d(double x, double y) {
        return new Point2D.Double(x, y);
    }
    
    //==========================================================================
    // static classes
    //==========================================================================
    
    public static class Builder {
        private double xmin = 0.0;
        private double ymax = 2.0;
        private double xmax = 4.;
        private double ymin = -2.;
        private int panelWidth = 400;
        private List<PietsFunction> functions = new ArrayList<>();
        private String title = "Unnamed";
        
        public Builder withTitle(String t) {
            title = t;
            return this;
        }
        
        public Builder withXmin(double xmin) {
            this.xmin = xmin;
            return this;
        }
        
        public Builder withXmax(double xmax) {
            this.xmax = xmax;
            return this;
        }
        
        public Builder withyMin(double y) {
            this.ymin = y;
            return this;
        }
        
        public Builder withyMax(double y) {
            this.ymax = y;
            return this;
        }
        
        public Builder withPanelwidth(int panelWidth) {
            this.panelWidth = panelWidth;
            return this;
        }
        
        public Builder withPietsFunction(PietsFunction f) {
            functions.add(f);
            return this;
        }
        
        public JFrame build() {
            try {
                var panel = new Grafiek(xmin, xmax, ymin, ymax, functions, panelWidth);
                var f = new JFrame(title);
                f.setContentPane(panel);
                f.pack();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setLocationRelativeTo(null);
                return f;
            }
            catch(NoninvertibleTransformException e) {
                var s = "the implied transformation-matrix is not invertible!";
                throw new RuntimeException(s);
            }
        }
    }
    
    //==========================================================================
    private static class Grafiek extends JPanel {
        
        // instance members
        private final List<PietsFunction> functions;
        private final Map<PietsFunction, Path2D> paths = new HashMap<>();
        
        private final Color light = new Color(225, 225, 225);
        private final Color dark = new Color(210, 210, 210);
        
        private final Point2D topLeft;
        private final Point2D bottomRight;
        
        private Path2D xaxis, yaxis;
        private Path2D horizontalMarker, verticalMarker;
        
        private final Point offset = new Point(50, 50);
        
        private final int viewWidth;
        private final int viewHeight;
        private final int panelWidth;
        private final int panelHeight;
        
        private final Point2D markerSize; // x = length long marker, y = length short marker
        
        private final AffineTransform userToPanel;
        private final AffineTransform panelToUser;
        
//        List<JLabel> axeslabels = new ArrayList<>();
        
        // constructor
        private Grafiek(double xmin, double xmax, double ymin, double ymax,
                List<PietsFunction> list, int panelWidth) throws NoninvertibleTransformException {
            //------------------------------------------------
            // null-layout
            this.setLayout(null);
            // ratio height / width
            var ratio = abs(ymax - ymin) / (xmax - xmin);
            // determine topLeft and bottomright
            this.topLeft = p2d(xmin, ymax);
            this.bottomRight = p2d(xmax, ymin);
            // viewWidth and viewhHeight
            this.viewWidth = panelWidth;
            this.viewHeight = (int) (viewWidth * ratio + 1);
            // real panelwidth & height have the offset added to vieww. & viewh.
            this.panelWidth = viewWidth + 2 * offset.x;
            this.panelHeight = viewHeight + 2 * offset.y;
            
            // store the supplied functions
            functions = new ArrayList<>(list);
            // set the preferredSize for this panel
            this.setPreferredSize(new Dimension(this.panelWidth, this.panelHeight));
            
            // determining the sizes of the markers, long and short
            var rathor = (xmax - xmin) / viewWidth;
            var ratver = (ymax - ymin) / viewHeight;
            var rat = max(rathor, ratver);
            markerSize = p2d(20 * rat, 10 * rat);
            
            // affinre transforms
            // userToPanel translates points in usercoordinates to real pixels
            // in the panel
            userToPanel = AffineTransformHelper.create(
                p2d(offset.x, offset.y), p2d(viewWidth + offset.x, viewHeight + offset.y), 
                topLeft, bottomRight, 
                true
            );
            // and vice-versa. if the inverse matricx exists!
            panelToUser = userToPanel.createInverse();
            
            // making Path2D's from the supplied functions
            createFunctionPaths();
            
            // and we create x- and y-axes, and de markers and labels
            // for the graph itself
            createAxesAndLabels();
        }
        
        private void createFunctionPaths() {
            try {
                for (var fnc: functions) paths.put(fnc, fnc.createPath(viewWidth));
            }
            catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    
        private void createAxesAndLabels() {
            // create x and y axis
            xaxis = new Path2D.Double();
            xaxis.moveTo(topLeft.getX(), 0);
            xaxis.lineTo(bottomRight.getX(), 0);
            
            yaxis = new Path2D.Double();
            yaxis.moveTo(0, topLeft.getY());
            yaxis.lineTo(0, bottomRight.getY());
            
            // create markers and labels
            var format = "%2.1f";
            var center = SwingConstants.CENTER;
            
            // horizontal marker, bottom of graphic
            horizontalMarker = new Path2D.Double();
            horizontalMarker.moveTo(topLeft.getX(), bottomRight.getY());
            horizontalMarker.lineTo(bottomRight.getX(), bottomRight.getY());
            var y = bottomRight.getY();
            for (int i = 0; i <= 10; i++) {
                var x = (topLeft.getX() * (10 - i) + bottomRight.getX() * i) / 10;
                var markerLength = i % 2 == 0 ? markerSize.getX() : markerSize.getY();
                horizontalMarker.moveTo(x, y);
                horizontalMarker.lineTo(x, y - markerLength);
                var label = new JLabel(format.formatted(x), center);
                label.setBorder(BorderFactory.createLineBorder(Color.black));
                var p = userToPanel.transform(p2d(x, y), null);
                label.setBounds((int) p.getX() - 20, (int)p.getY() + 20, 40, 20);
                this.add(label);
            }
            
            // vertical marker, 
            verticalMarker = new Path2D.Double();
            verticalMarker.moveTo(topLeft.getX(), topLeft.getY());
            verticalMarker.lineTo(topLeft.getX(), bottomRight.getY());
            var x = topLeft.getX();
            for (int i = 0; i <= 10; i++) {
                y = (topLeft.getY() * (10 - i) + bottomRight.getY() * i) / 10;
                var markerLength = i % 2 == 0 ? markerSize.getX() : markerSize.getY();
                verticalMarker.moveTo(x, y);
                verticalMarker.lineTo(x - markerLength, y);
                var label = new JLabel(format.formatted(y), center);
                var p = userToPanel.transform(p2d(x, y), null);
                label.setBounds((int) p.getX() - 40, (int) p.getY(), 40, 20);
                this.add(label);
            }
            
            // names of the functions
            
            int xposition = 20, gap = 20;
            for (var e: functions) {
                var len = e.getName().length() * 8;
                var label = new JLabel(e.getName());
                label.setForeground(e.getColor());
                label.setBounds(xposition, 20, len, 20);
                this.add(label);
                xposition += len + gap;
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            for (int i = 0; i < 10; i++) {
                var xL = offset.x;
                var yL = offset.y + i * viewHeight / 10;
                var c = i % 2 == 0 ? light : dark;
                g.setColor(c);
                g.fillRect(xL, yL, viewWidth, viewHeight / 10);
            }
            var g2d = (Graphics2D) g.create();
            g2d.setTransform(userToPanel);
            g2d.setStroke(new BasicStroke(0.0f));
            g2d.setColor(Color.BLACK);
            g2d.draw(horizontalMarker);
            g2d.draw(verticalMarker);
            g2d.draw(xaxis);
            g2d.draw(yaxis);
            var x = min(topLeft.getX(), bottomRight.getX());
            var y = min(topLeft.getY(), bottomRight.getY());
            var w = abs(topLeft.getX() - bottomRight.getX());
            var h = abs(topLeft.getY() - bottomRight.getY());
            var rect = new Rectangle2D.Double(x, y, w, h);
            g2d.clip(new Rectangle2D.Double(x, y, w, h));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var pixel = AffineTransformHelper.pixelsize(userToPanel);
            g2d.setStroke(new BasicStroke(2 * (float)pixel));
            for (var e: paths.entrySet()) {
                g2d.setColor(e.getKey().getColor());
                g2d.draw(e.getValue());
            }
            g2d.dispose();
//            g2d.setStroke(new BasicStroke(0.0f));
//            for (var e: paths.entrySet()) {
//                g2d.setColor(e.getKey().color());
//                g2d.draw(e.getValue());
//            }
        }
    }
    
    //*******************************************************************
    
    public final static class FourierSeries {

        private double constant = 0;
        private BiFunction<Double, Integer, Double> cosTerm = (x, n) -> 0d;
        private BiFunction<Double, Integer, Double> sinTerm = (x, n) -> 0d;
        private static final HashMap<Integer, BigInteger> facs = new HashMap<>();
        
        {
            facs.put(0, BigInteger.ONE);
            for (int i = 1; i <= 50; i++) facs.put(i, facs.get(i - 1).multiply(new BigInteger("" + i)));
        }

        private FourierSeries() {}
        
        public static FourierSeries getInstance() {
            return new FourierSeries();
        }
        
        public FourierSeries withConstant(double constant) {
            this.constant = constant;
            return this;
        }

        public FourierSeries withCosTerm(BiFunction<Double, Integer, Double> cosTerm) {
            this.cosTerm = cosTerm;
            return this;
        }

        public FourierSeries withSinTerm(BiFunction<Double, Integer, Double> sinterm) {
            this.sinTerm = sinterm;
            return this;
        }

        public double apply(double x, int nrOfCosSinTerms) {
            var result = IntStream.rangeClosed(1, nrOfCosSinTerms)
                    .boxed()
                    .flatMapToDouble(i -> DoubleStream.of(cosTerm.apply(x, i), sinTerm.apply(x, i)))
                    .sum()
                    + constant / 2
            ;
            return result;
        }
    }
}

//==============================================================================
abstract class PietsFunction<T> {
    final Function<Double, T> f;
    final String name;
    final Color c;
    final double argMin, argMax;
    
    PietsFunction(Function<Double, T> f, String name, Color c, double argMin, double argMax) {
        this.f = f;
        this.name = name;
        this.c = c;
        this.argMin = argMin;
        this.argMax = argMax;
    }
    
    public Function<Double, T> getFunction() {
        return f;
    }
    
    public String getName() {
        return name;
    }
    
    public Color getColor() {
        return c;
    }
    
    abstract Path2D createPath(int nrOfPoints);
}

//==============================================================================
/**
 * represtens a function from R1 into R2
 * @author Piet
 */
class FunctionNameColorXYfT extends PietsFunction<Point2D> {
    
    FunctionNameColorXYfT(Function<Double, Point2D> f, String name, Color c, double tmin, double tmax) {
        super(f, name, c, tmin, tmax);
     
    }
    
    public Function<Double, Point2D> getFunction() {
        return f;
    }
    
    public Path2D createPath(int nrOfPoints) throws RuntimeException {
        var result = new Path2D.Double();
        for (int i = 0; i <= nrOfPoints; i++) {
            var t = argMin * (1 - i) + argMax * i;
            var point = f.apply(t);
            var x = point.getX();
            var y = point.getY();
            if (i == 0) result.moveTo(x, y);
            else result.lineTo(x, y);
        }
        return result;
    }
}

//==============================================================================
/**
 * represents a Function in polar coordinates: r = f(phi)
 * @author Piet
 */
class FunctionNameColorRfPhi extends PietsFunction<Double> {
    
    FunctionNameColorRfPhi(Function<Double, Double> f, String name, Color c, double phiMin, double phiMax) {
        super(f, name, c, phiMin, phiMax);
    }
    
    public Function<Double, Double> getFunction() {
        return f;
    }
    
    public Path2D createPath(int nrOfPoints) throws RuntimeException {
        var result = new Path2D.Double();
        for (int i = 0; i <= nrOfPoints; i++) {
            var phi = (argMin * (nrOfPoints - i) + argMax * i) / nrOfPoints;
            var r = f.apply(phi);
            var x = r * cos(phi);
            var y = r * sin(phi);
            if (i == 0) result.moveTo(x, y);
            else result.lineTo(x, y);
        }
        return result;
    }
}

//==============================================================================
/**
 * represents a Function in polar coordinates: r = f(phi)
 * @author Piet
 */
class FunctionNameColorYfX extends PietsFunction<Double> {
    
    FunctionNameColorYfX(Function<Double, Double> f, String name, Color c, double xmin, double xmax) {
        super(f, name, c, xmin, xmax);
    }
    
    public Function<Double, Double> getFunction() {
        return f;
    }
    
    @Override
    public Path2D createPath(int nrOfPoints) throws RuntimeException {
        var result = new Path2D.Double();
        for (int i = 0; i <= nrOfPoints; i++) {
            var x = (argMin * (nrOfPoints - i) + argMax * i) / nrOfPoints;
            var y = f.apply(x);
            if (i == 0) result.moveTo(x, y);
            else result.lineTo(x, y);
        }
        return result;
    }
}

//==============================================================================
class AffineTransformHelper {
    
   public static void main(String... args) {
      int width = 500, height = 500;
      AffineTransform at = create(0, 0, width - 1, height - 1, -1, 1, 1, 2, false);
      System.out.println(at);
      System.out.println(pixelsize(at));
   }
 
       /**
     * This method creates an AffineTransform that will help to
     * translate between two coordinatesets. The coordinateset to which
     * the inputcoordinates should be translated, is specified by two
     * inputparameters: Point2D ToTopLeft and Point2D ToBottomRight.
     * Likewise, the coordinatesystem from which the coordinates should
     * be transformed are specified by two Point2D as well, and are called
     * FromTopLeft and FromBottomRight.
     * 
     * An example of its usage: say you have a JPanel with topleft 
     * coordinates (0, 0) and bottomright (400, 400).
     * 
     * And say that we want to use usercoordinates in a rectangle (0, 1) 
     * (topleft) and (2,0) bottomright.
     * 
     * Now, the aspectratio of both (height / width) differ: for the panel it
     * is Par = 1, and for the userarea it is Uar = 1 / 2 = .5.
     * If we do nothing about this, then a circle in userplane will become
     * an ellips in the panel. There is a fifth parameter that indicates
     * whether we should take this into account or not. This parameter
     * is the boolean 'keepAspectratio. If true, then a circle in userplane
     * will be a circle as well in the panel.
     * In 'main' an example is given
     * 
     * @param ToTopLeft a Point2D indicating the topleft point of the TO area
     * @param ToBottomRight a Point2D indicating the bottomleft point of the TO area
     * @param FromTopLeft a Point2D indicating the topleft point of the FROM area
     * @param FromBottomRight a Point2D indicating the bottomright point of the FROM area
     * @param keepAspectratio boolean. If true then a coorection will be made
     *                        in case of different aspectratios
     * @return an AffineTransform to do the coordinate change
     */
    public static AffineTransform create(
            Point2D ToTopLeft, Point2D ToBottomRight,
            Point2D FromTopLeft, Point2D FromBottomRight,
            boolean keepAspectratio
        ) {
        
        double TL = ToTopLeft.getX(),   TR = ToBottomRight.getX(), 
               TT = ToTopLeft.getY(),   TB = ToBottomRight.getY(),
               FL = FromTopLeft.getX(), FR = FromBottomRight.getX(),
               FT = FromTopLeft.getY(), FB = FromBottomRight.getY()
        ;
        
        if (keepAspectratio) {
            double Par = abs((TT - TB) / (TR - TL));
            double Uar = abs((FT - FB) / (FR - FL));
            if (Uar > Par) {
                // then increase FL and FR
                double lengthToBe = Uar / Par * abs(FR - FL);
                double correction = (lengthToBe - (FR - FL)) / 2;
                FR += correction;
                FL -= correction;
            }
            else {
                // increase FT and FB
                double lengthToBe = Par / Uar * abs(FT - FB);
                double correction = (lengthToBe - (FT - FB)) / 2;
                FT += correction;
                FB -= correction;
            }
        }
        double alpha = (TL - TR) / (FL - FR);
        double beta = 0, gamma = 0;
        double delta = (TT - TB) / (FT - FB);
        double e = TL - alpha * FL;
        double f = TB - delta * FB;
        return new AffineTransform(alpha, gamma, beta, delta, e, f);
    }
    
    /**
     * like the create constructor above, only the parameters are not points
     * but the individual coordinates of these points
     * @param toLeft          the left x coordinate of the TO area
     * @param toTop           the upper y coordinate of the TO area
     * @param toRight         the right x coordinate of the TO area
     * @param toBottom        the bottom y coordinate of the TO area
     * @param fromLeft        the left x coordinate of the FROM area
     * @param fromTop         the upper y coordinate of the FROM area
     * @param fromRight       the right x coordinate of the FROM area
     * @param fromBottom      the bottom y coordinate of the FROM area
     * @param keepAspectratio if true, takes care that TO and FROM have 
     *                        the same aspectratio
     * @return 
     */
    public static AffineTransform create( 
        double toLeft, double toTop, double toRight, double toBottom,
        double fromLeft, double fromTop, double fromRight, double fromBottom,
        boolean keepAspectratio
        ) {
        var toTopLeft = new Point2D.Double(toLeft, toTop);
        var toBottomRight = new Point2D.Double(toRight, toBottom);
        var fromTopLeft = new Point2D.Double(fromLeft, fromTop);
        var fromBottomRight = new Point2D.Double(fromRight, fromBottom);        
        return create(toTopLeft, toBottomRight, fromTopLeft, fromBottomRight, keepAspectratio);      
    }
    
    /**
    * gives the pixelsize of a unit after AffineTransform 'at' is applied
    * @param at the AffineTransform in question
    * @return doube number of pixels of one unit in 'at' coordinates
    */
    public static double pixelsize(AffineTransform at) {
        double scalex = Math.abs(at.getScaleX());
        double scaley = Math.abs(at.getScaleY());
        if (scalex != 0.0) return 1 / scalex;
        else if (scaley != 0.0) return 1 / scaley;
        else return 0;
    }
    
   /**
    * given an AffineTransform at, calculates another AffineTransform to be used
    * in deriving a font tha is suitable for the scaling in at.
    * Say, we have an at, and a font f. Now, the usage is:
    * AffineTrnansform atfont = AffineTransform.createFontTransform(at);
    * fontToUse = f.deriveFont(atfont);
    * and use fontToUse in your drawing routines
    * @return see above
    */
    public static AffineTransform createFontTransform(AffineTransform at) {
        double scaleX = at.getScaleX(), scaleY = at.getScaleY();
        return AffineTransform.getScaleInstance(1 / scaleX, 1 / scaleY);
    }

}


