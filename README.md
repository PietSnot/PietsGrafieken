# PietsGrafieken
an easy way to show graphs in java
an example how to use it:

public static void main(String[] args) {
    var fs = PietsGrafieken.FourierSeries.getInstance()
            .withConstant(1)
            .withCosTerm((x, n) -> 0d)
            .withSinTerm((x, n) -> n % 2 == 0 ? 0d : sin(n * x) * -2 / (n * PI));
    ;

    // show the frame with these functions in a frame:
    new PietsGrafieken.Builder()
        .withTitle("test")
        .withPanelwidth(800)
        .withXmin(-12)
        .withXmax(10)
        .withyMin(-2)
        .withyMax(5)
        .withFunctionNameColor(d -> sin(d) * cos(2d), "sinx * cos2x", Color.red)
        .withFunctionNameColor(d -> fs.apply(d, 5), "fourier n = 5", Color.BLUE)
        .withFunctionNameColor(d -> fs.apply(d, 20), "fourier n = 20", Color.BLACK)
        .build()
        .setVisible(true);
    ;  
}
