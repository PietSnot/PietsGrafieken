package pietsgrafieken;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Double.min;
import static java.lang.Math.PI;
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
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This class enables the easy projection of one or more functions.
 * @author Piet
 */
public class PietsGrafieken {
    
    /**
     * here you find an example of how to use PietsGrafieken.
     * 
     * @param args the command-line arguments, not needed
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int nrOfPoints = 600;
        var f = new FunctionNameColorYfX(d -> 6 * Math.exp(-.5 * d * d) / Math.pow(2 * PI, .5), "normal distr. (*6)", Color.red, -5, 5);
        var g = new FunctionNameColorRfPhi(d -> 5 * cos(3 * d), "r = 5 * cos(phi * 3)", Color.BLUE, 0, 2 * PI);
        var fs = PietsGrafieken.FourierSeries.getInstance()
                .withConstant(0)
                .withSinTerm((x, n) -> n % 2 == 0 ? -2.0 / n * sin(n * x) : 2.0 / n * sin(n * x))
        ;
        
        var fun = new FunctionNameColorYfX(x -> fs.apply(x, 30), "fourier with 30 terms", new Color(255, 0, 255), -3 * PI, 3 * PI);  
        
        var h = new FunctionNameColorYfX(d -> fs.apply(d, 10), "fourier with 10 terms", Color.black, -3 * PI, 3 * PI);
        var j = new FunctionNameColorXYfT(d -> new Point2D.Double(d * cos(d), sin(d)), "some sin/cos function", Color.MAGENTA, -3, 3);
        new PietsGrafieken.Builder()
            .withPanelwidth(nrOfPoints)
            .withTitle("Piets Grafieken - test")
            .withXmin(-5)
            .withyMax(5)
            .withXmax(5)
            .withyMin(-5)
            .withPietsFunction(fun)
            .withPietsFunction(f)
            .withPietsFunction(g)
            .withPietsFunction(h)
            .withPietsFunction(j)
            .build()
            .setVisible(true)
        ;
    }
    
    /**
     * no public constructor. Use new PietsGrafieken.Builder()
     * 
     */
    private PietsGrafieken() {}
    //==========================================================================
    //  static methods
    //==========================================================================
    /**
     * a helper function, that saves a lot of typing, Instead of
     * <ul>
     * <li>var p = new Point2D.Double(x,y);</li>
     * <li>var p = p2d(x, y);</li>
     * </ul>
     * @param x a double
     * @param y a double
     * @return a Point2D.Double(x, y)
     */
    public static Point2D p2d(double x, double y) {
        return new Point2D.Double(x, y);
    } 
    
    //==========================================================================
    // static classes
    //==========================================================================
    /**
     * class Builder is an easy way to create the graph. It has for any
     * parameter xx a .withXX-method
     */
    public static class Builder {
        private double xmin = 0.0;
        private double ymax = 2.0;
        private double xmax = 4.;
        private double ymin = -2.;
        private int panelWidth = 400;
        private List<PietsFunction> functions = new ArrayList<>();
        private String title = "Unnamed";
        
        /**
         * constructor
         * 
         */
        
        public Builder() {}
        
        /**
         * gives the graph a title
         * @param t String, the title
         * @return this Builder, so you can do method chaining 
         */
        public Builder withTitle(String t) {
            title = t;
            return this;
        }
        
        /**
         * determines the minimum x of the graph
         * @param xmin a double
         * default = 0
         * @return Builder, namely this, so that these 
         * with-methods can be chained
         */
        public Builder withXmin(double xmin) {
            this.xmin = xmin;
            return this;
        }
        
        /**
         * determines the maximum x of the graph
         * @param xmax a double
         * default = 4;
         * @return Builder, namely this, so that these 
         * with-methods can be chained
         */
        public Builder withXmax(double xmax) {
            this.xmax = xmax;
            return this;
        }
        
        /**
         * determines the minimum y of the graph
         * @param y a double
         * default = -2
         * @return Builder, namely this, so that these 
         * with-methods can be chained
         */
        public Builder withyMin(double y) {
            this.ymin = y;
            return this;
        }
        
        /**
         * determines the maximum y of the graph
         * @param y a double
         * default = 2;
         * @return Builder, namely this, so that these 
         * with-methods can be chained
         */
        public Builder withyMax(double y) {
            this.ymax = y;
            return this;
        }
        
        /**
         * gives the width of the graph-panel in pixels
         * this value also determines the number of points
         * calculated for each function within the given bounds
         * @param panelWidth the witdth of the graph
         * @return this, so that these with-metods can be chained
         */
        public Builder withPanelwidth(int panelWidth) {
            this.panelWidth = panelWidth;
            return this;
        }
        
        /**
         * adds a PietsFunction to the list of PietsFunctions
         * @param f a PietsFunction
         * @return this, so that these with-metods can be chained
         */
        public Builder withPietsFunction(PietsFunction f) {
            functions.add(f);
            return this;
        }
        
        /**
         * transforms this Builder into a JFrame, ready to be displayed
         * @return a JFrame containing the graph
         */
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
    /**
     * this is the graph that contains all the functions, their names
     * and a button to make a sna[pshot
     * It is private, so it can only be created with a Builder from
     * PietsGrafieken
     */
    private static class Grafiek extends JPanel {
        
        // user home directory
        private final JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home")));
        {
            var ffs = jfc.getChoosableFileFilters();
            for (var f: ffs) jfc.removeChoosableFileFilter(f);
            jfc.setFileFilter(new FileNameExtensionFilter("png", "png"));
        }
        
        // instance members
        private final List<PietsFunction> functions;
        private final Map<PietsFunction, Path2D> paths = new HashMap<>();
        
        private PietsFunction currentPietsFunction;
        
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
        
        private JPanel graphPanel;
        private JPanel buttonPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 20, 20));
        private JLabel coordinatesLabel = new JLabel("coordinates");
        private JTextField coordinatesTF = new JTextField(12);
        private Border border = BorderFactory.createLineBorder(Color.BLACK);
        
        Map<JLabel, PietsFunction> labelToPF = new HashMap<>();
        Map<PietsFunction, JLabel> pfToLabel = new HashMap<>();
        
        private final Point2D markerSize; // x = length long marker, y = length short marker
        
        private final AffineTransform userToPanel;
        private final AffineTransform panelToUser;
        
        // constructor
        private Grafiek(double xmin, double xmax, double ymin, double ymax,
                List<PietsFunction> list, int panelWidth) throws NoninvertibleTransformException {
            //------------------------------------------------
            
            graphPanel = new JPanel(null) {
                @Override
                public void paintComponent(Graphics g) {
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
                    g2d.clip(rect);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    var pixel = AffineTransformHelper.pixelsize(userToPanel);
                    g2d.setStroke(new BasicStroke(2 * (float)pixel));
                    for (var e: paths.entrySet()) {
                        g2d.setColor(e.getKey().getColor());
                        g2d.draw(e.getValue());
                    }
                    if (currentPietsFunction != null) {
                        g2d.setColor(currentPietsFunction.getColor());
                        g2d.draw(paths.get(currentPietsFunction));
                    }
                    g2d.dispose();
                }
            };
            
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
            
            // set the preferredSize of graphPanel
            graphPanel.setPreferredSize(new Dimension(this.panelWidth, this.panelHeight));
            
            // store the supplied functions
            functions = new ArrayList<>(list);
            
            // set cuurentPietsFunction
            if (!functions.isEmpty()) currentPietsFunction = functions.getFirst();
            
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
            
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            
            this.add(graphPanel);
            this.add(buttonPanel);
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
//                label.setBorder(border);
                var p = userToPanel.transform(p2d(x, y), null);
                label.setBounds((int) p.getX() - 20, (int)p.getY() + 20, 40, 20);
                graphPanel.add(label);
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
                graphPanel.add(label);
            }
            
            // snapshot button
            var str = "snapshot";
            var button = new JButton(str);
            button.addActionListener(this::processSnapshot);
            buttonPanel.add(button);
            
            // coordinates
            coordinatesLabel.setBounds(20, 20, 11 * 8, 20);
            coordinatesTF.setBounds(150, 20, 11 * 8, 20);
            coordinatesLabel.setVisible(true);
            coordinatesTF.setVisible(true);
            graphPanel.add(coordinatesLabel);
            graphPanel.add(coordinatesTF);
            
            
            var m = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent me) {
                    var cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                    setCursor(cursor);
                    var format = "( %5.2f, %5.2f )";
                    var p = me.getPoint();
                    var pd = panelToUser.transform(p2d(p.x, p.y), null);
                    coordinatesTF.setText(format.formatted(pd.getX(), pd.getY()));
                }
                @Override
                public void mouseMoved(MouseEvent me) {
                    var format = "( %5.2f, %5.2f )";
                    var p = me.getPoint();
                    var pd = panelToUser.transform(p2d(p.x, p.y), null);
                    coordinatesTF.setText(format.formatted(pd.getX(), pd.getY()));
                }
                @Override
                public void mouseExited(MouseEvent me) {
                    coordinatesTF.setText("");
                    setCursor(Cursor.getDefaultCursor());
                }
            };
            
            graphPanel.addMouseListener(m);
            graphPanel.addMouseMotionListener(m);
            
            // names of the functions
            for (var e: functions) {
                var label = new JLabel(e.getName());
                labelToPF.put(label, e);
                pfToLabel.put(e, label);
                label.setForeground(e.getColor());
                if (e.equals(currentPietsFunction)) label.setBorder(border);
                MouseAdapter me = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent me) {
                        var label = (JLabel) me.getSource();
                        if (currentPietsFunction != null) {
                            pfToLabel.get(currentPietsFunction).setBorder(null);
                            currentPietsFunction = labelToPF.get(label);
                            label.setBorder(border);
                            graphPanel.repaint();
                        } 
                    }
                };
                label.addMouseListener(me);
                buttonPanel.add(label);
            }
        }
         
        @Override
        public void paint(Graphics g) {
            paintChildren(g);
        }
        
        private void processSnapshot(ActionEvent ae) {
            var approve = jfc.showSaveDialog(this);
            if (approve != JFileChooser.APPROVE_OPTION) return;
            var file = jfc.getSelectedFile();
            if (file.exists()) {
                var doit = JOptionPane.showConfirmDialog(
                    this,
                    "this file exists already. Still save?",
                    "Exists!!", 
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (doit == JOptionPane.CANCEL_OPTION) return;
            }
            var filename = file.getAbsolutePath();
            var extension = file.getName().endsWith("png");
            if (!extension) file = new File(filename + ".png");
            var buf = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
            this.paint(buf.getGraphics());
            try {
                ImageIO.write(buf, "png", file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "could not save the file. Try again", "Sorry!!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    //*******************************************************************
    /**
     * a class inside PietsGrafieken
     * <p>It is about making a Function that produces a part of 
     * a so called Fourier series.</p>
     * <p>A Fourier series is a mixture of a constant term, and an infinite
     * series of sine- and/or cosine-terms.</p>
     * <p> Fourier series here has three parts:
     * <ol>
     * <li>    a constant term, a double </li>
     * <li>    a sine-term. This is a BiFunction&lt;Double, Integer, Double&gt;</li>
     * <li>    a cosine-term. This is a BiFunction&lt;Double, Integer, Double&gt;</li>
     * </ol>
     * <p>The Integer denotes how many of the infinite terms we take into account.
     * The higher this value, the more accurate the approach is</p>
     * <p>
     * For instance: let f(x) = 0, -PI &lt; x &lt; 0, f(x) = x 0 &lt; x &lt; PI, with a 
     * period of 2PI.</p>
     * 
     * <p>The corresponding Fourier-series has a constant term of PI / 2
     * <br>the cosine-terms have two parameters: the input value x and the
     * <br>number n denoting the nth cosine-term. In this case we have:</p>
     * <p><br>costerm(x, n) = 0 if n even, otherwise = -2 / (PI * n * n) * cos(n * x)
     * <br>sinterm(x, n) = -1 / n * sin(n * x) if n even, = 1 / n * sin(n * x) n odd.
     * </p>
     * <p>We can create an instance with:</p>
     * 
     * <p><code>var fourier = PietsGrafieken.Fourierseries.getInstance()
     * <br>    .withConstant(PI / 2)
     * <br>    .withCosTerm((x, n) -> n % 2 == 0 ? 0 : -2 / (PI * n * n) * cos(n * x))
     * <br>    .withSinTerm((x, n) -> n % 2 == 0 ? -1 / n * sin(n * x) : 1 / n * sin(n * x))
     * <br>;
     * </code></p>
     * <p>To get a Graph we must convert this to a Function y = fx by fixing
     * some value for n, the number of terms to take into account. The type to
     * use is a PietsFunction, in this case the plain y = f(x) form:</p>
     * 
     * <p><code>BiFunction&lt;Double, Double&gt;  fs = x -> fourier.apply(x, 20);</code></p>
     * 
     * <p>Here we take the first 20 terms of the sine and cosine terms.</p>
     * 
     * <p><code>var fs = new PietsFunctionYfX(fs, "fourier with 20 terms", Color.BLUE, -3 * PI, 3 * PI);</code></p>
     * 
     * <p>And we can view this with for instance:</p>
     * 
     * <p><code>new PietsGrafieken.Builder()
     * <br>    .withPanelWidth(600)
     * <br>    .withTitle("FourierSeries with 20 sine and cosine-terms")
     * <br>    .withXmin(-3 * PI)
     * <br>    .withyMax(3 * PI(
     * <br>    .withXmax(3 * PI)
     * <br>    .withYmin(-3 * PI)
     * <br>    .withPietsFunction(fs)
     * <br>    .build()
     * <br>    .setVisible(true)
     * <br>;</code></p>
     * 
     */
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
        
        /**
         * use this to get an instance
         * @return instance of a FourierSeries
         */
        public static FourierSeries getInstance() {
            return new FourierSeries();
        }
        
        /**
         * sets the constant of this series
         * @param constant a double
         * @return this instance, so that you can chain the terms
         */
        public FourierSeries withConstant(double constant) {
            this.constant = constant;
            return this;
        }

        /**
         * the cos-terms of the series
         * @param cosTerm a a BiFunction&lt;Double, Integer, Double>
         *        representing the input x asnd the number of the term
         * @return this instance, so that you can chain the settings
         */
        public FourierSeries withCosTerm(BiFunction<Double, Integer, Double> cosTerm) {
            this.cosTerm = cosTerm;
            return this;
        }

        /**
         * the sin-terms of the series
         * @param sinterm a a BiFunction&lt;Double, Integer, Double>
         *        representing the input x asnd the number of the term
         * @return this instance, so that you can chain the settings
         */
        public FourierSeries withSinTerm(BiFunction<Double, Integer, Double> sinterm) {
            this.sinTerm = sinterm;
            return this;
        }

        /**
         * calculates the value of the series for input x and the total
         * number of sin- and cos-terms to take into account
         * @param x  the doubler inputvalue
         * @param nrOfCosSinTerms the number of terms to take into account
         * @return value =
         * constant term / 2 +
         * for (int i = 1, ..., nrOfTerms) costerm(x, i) +
         * for (int i = 1, .... nrOfTerms) sinterm(x, i)
         */
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
    
    //==============================================================================
    /**
     * this abstract class is the basis of the possible Pietsfunctions
     * 
     * @author Piet
     */
    public abstract static class PietsFunction<T> {
        final Function<Double, T> f;
        final String name;
        final Color c;
        final double argMin, argMax;

        public PietsFunction(Function<Double, T> f, String name, Color c, double argMin, double argMax) {
            this.f = f;
            this.name = name;
            this.c = c;
            this.argMin = argMin;
            this.argMax = argMax;
        }

        /**
         * @return the defined function
         */
        public Function<Double, T> getFunction() {
            return f;
        }

        /**
         * 
         * @return String, the defined name
         */
        public String getName() {
            return name;
        }

        /**
         * 
         * @return a Color, the defined color
         */
        public Color getColor() {
            return c;
        }
        
        /**
         * 
         * @return Point2D, containing the minimum and maximum value of the domain
         */
        public Point2D getDomain() {
            return p2d(argMin, argMax);
        }

        /**
         * the method that creates a Path2D of the function and its doamin
         * 
         * @param nrOfPoints the number of points within the domain for which the function will be calculated
         * 
         * @return 
         */
        public abstract Path2D createPath(int nrOfPoints);
    }
    
    //==============================================================================
    /**
     * represtens a function from R1 into R2
     * @author Piet
     */
    /**
     * this is a subclass of the basis PietsFunction, with &lt;T> being a Point2D
     * It represents the litteral function: t -> Point2D.Double(f(t), g(t))
     * It is a funbction from R1 to R2
     * @author Piet
     */
    public static class FunctionNameColorXYfT extends PietsFunction<Point2D> {

        /**
         * constructor
         * @param f a function&lt;Double, Point2D&gt; the function to show
         * @param name a String, the name of this function in the graph
         * @param c a Color, thwe color of this function in the graph
         * @param tmin the minimum value of t for which the graph is calculated
         * @param tmax the maximum value of t for which the graph is calculated
         * 
         * <p> an example of its use:</p>
         * <pre>
         * var f = new FunctionNameColorXYfT(d -> -d, "y = -x", Color.BLUE, -4, 4);
         * </pre>
         */
        public FunctionNameColorXYfT(Function<Double, Point2D> f, String name, Color c, double tmin, double tmax) {
            super(f, name, c, tmin, tmax);
        }

        /**
         * creates a Path2D, by dividing the supplied segment into 'nrOfPoints'
         * points and calculating the value of all these points
         * 
         * @param nrOfPoints integer, denoting the number of points for which
         * a value will be calculated
        */
        public Path2D createPath(int nrOfPoints) throws RuntimeException {
            var result = new Path2D.Double();
            for (int i = 0; i <= nrOfPoints; i++) {
                var t = (argMin * (nrOfPoints - i) + argMax * i) / nrOfPoints;
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
     * <pre>The point (x,y) is then calculated as
     *     . x = r * cos(phi)
     *     . y = r * sin(phi)
     * </pre>
     * @author Piet
     */
    public static class FunctionNameColorRfPhi extends PietsFunction<Double> {

        /**
         * constructor
         * @param f  the Function&lt;Double, Double> r = f(phi)
         * @param name the name of this function, displayed in the graph
         * @param c  the color of this function in the graph
         * @param phiMin double, the minimum value for which the function will be applied
         * @param phiMax ouble, the maximum value for which the function will be applied
         * 
         * <pre>
         * use: var f = new FunctionNameColorRfPhi(
         *          fi -> 3 * cos(fi * 3, 
         *          "r = 3cos(phi)",
         *          Color.BLUE,
         *          -3 * PI,
         *           3 * PI
         *      );
         * </pre>
         */
        public FunctionNameColorRfPhi(Function<Double, Double> f, String name, Color c, double phiMin, double phiMax) {
            super(f, name, c, phiMin, phiMax);
        }

        /**
         * creates a Path2D.Double, by dividing the supplied domain
         * into the supplied nrOfPoints and calculatoing the value of
         * each of those points.
         * @param nrOfPoints  int, the number of subdivisions of the domain
         * @return Path2D
         * @throws RuntimeException 
         */
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
    /**
     * this clas represents a normal y = f(x) function
     * @author Piet
     */
    public static class FunctionNameColorYfX extends PietsFunction<Double> {
        /**
         * constructor
         * @param f   a Function&lt;Double, Double>
         * @param name  String, the name of this function
         * @param c  the color of this function in the graph
         * @param xmin double
         * @param xmax double
         * the domain of this function = [xmin, xmax]
         */
        FunctionNameColorYfX(Function<Double, Double> f, String name, Color c, double xmin, double xmax) {
            super(f, name, c, xmin, xmax);
        }

        /**
         * creates a Path2D.Double, by dividing the supplied domain
         * into the supplied nrOfPoints and calculatoing the value of
         * each of those points.
         * @param nrOfPoints  int, the number of subdivisions of the domain
         * @return Path2D
         * @throws RuntimeException if one of the domain points
         * results in an ArithmeticException
         */
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
    /**
     * This class creates AffineTransform in an easy way. An AffineTransform
     * lets you easily switch from one coordinate system to another.
     * 
     * @author Piet Muis, Den Haag Holland, 20-08-2025
     */
    public static class AffineTransformHelper {

        private AffineTransformHelper() {}
        
        /**
         * just a main, for testing purposes and a description
         * 
         * @param args the arguments
         */
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
         * the call is then as follows:
         * var at = create(p2d(0, 0), p2d(400, 400), p2d(0, 1), p2d(2, 0), true);
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
         * The example given in the other constructor would be
         * var at = create(0, 0, 400, 400, 0, 1, 2, 0, true);
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
         * @return the correct AffineTransform
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
        * @return double number of pixels of one unit in 'at' coordinates
        * 
        * use for instance:
        * // g2d is the relevant Graphics2D, userToPanel is the AffineTransform
        * that maps user coordinates to JPanel coordinates
        * var pixel = AffineTransformHelper.pixelsize(userToPanel);
        * g2d.setStroke(new BasicStroke(2 * (float)pixel));
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
        * AffineTransform atfont = AffineTransform.createFontTransform(at);
        * fontToUse = f.deriveFont(atfont);
        * and use fontToUse in your drawing routines
        * 
        * @param at AffineTransform
        * @return see above
        */
        public static AffineTransform createFontTransform(AffineTransform at) {
            double scaleX = at.getScaleX(), scaleY = at.getScaleY();
            return AffineTransform.getScaleInstance(1 / scaleX, 1 / scaleY);
        }
    }

}


