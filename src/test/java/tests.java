import eu.mihosoft.jcsg.ext.path.SVGPath;
import org.junit.Test;

public class tests {

    String path = "m 44.60119,85.333332 c 7.989715,-8.571545 26.84315,-8.29381 29.982173,-21.890636 4.287461,-9.040619 -1.116979,-25.80226 8.789046,-29.988125 19.613851,-3.047385 41.674431,2.373924 54.198871,18.651219 5.47716,6.815747 4.80118,13.009835 -0.17694,22.266232 -5.33515,12.902386 5.65176,30.316308 15.72661,41.123788 9.39009,11.59428 25.61962,17.69247 30.58249,32.65285 1.85847,11.3612 1.14769,23.49796 -2.69912,34.40674 -5.46654,14.84296 -21.89953,20.34395 -36.04556,23.18679 -17.71696,3.46927 -35.63892,6.04663 -53.53514,8.26151 -15.387863,1.04112 -33.096108,0.67095 -44.526702,-11.31777 C 32.693202,187.63749 27.238356,166.86608 22.378749,147.31557 18.594896,130.20764 21.805982,111.29244 33.263521,97.688679 36.711597,93.279609 40.614919,89.25334 44.60119,85.333332 Z";

    @Test
    public void testPathAutoReverse() {
        // extrude the path
        SVGPath.toCSG(
                path, // svg path
                10.0, //    extrusion height
                0.01, //    step size
                2.0); //    extension
    }

}