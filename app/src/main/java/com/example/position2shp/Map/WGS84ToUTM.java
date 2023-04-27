package com.example.position2shp.Map;

/**
 * Created by Franziska on 06.03.2018.
 */

public class WGS84ToUTM {

    final double a = 6378137.0;
    final double b = 6356752.31424;
    final double f = 1/298.25722;
    final double e = Math.sqrt((Math.pow(a, 2) - Math.pow(b, 2))/Math.pow(a, 2));
    final double eDash = f / b;
    final double k0 = 0.9996;
    final double capE = (a*a - b*b) / (b*b); //Math.pow(e, 2)/(1 - Math.pow(e, 2));
    final double c = Math.pow(a, 2) / b;

    private double m(double phi)
    {
       /*double a1 = c * (1 - (3/4) * Math.pow(eDash, 2) + (45/64) * Math.pow(eDash, 4) - (175/256) * Math.pow(eDash, 6) + (11025/16384) * Math.pow(eDash, 8));
        double a2 = c * (- (3/8) * Math.pow(eDash, 2) + (15/32) * Math.pow(eDash, 4) - (525/1024) * Math.pow(eDash, 6) + (2205/4096) * Math.pow(eDash, 8));
        double a3 = c * ((15/256) * Math.pow(eDash, 4) - (105/1024) * Math.pow(eDash, 6) + (2205/16384) * Math.pow(eDash, 8));
        double a4 = c * (- (35/3072) * Math.pow(eDash, 6) + (315/12288) * Math.pow(eDash, 8));
        double g = a1 * degToRad(phi) + a2 * Math.sin(2 * phi) + a3 * Math.sin(4 * phi) + a4 * Math.sin(6 * phi);*/
       double g = a * ((1 - Math.pow(e, 2)/4. - 3.* Math.pow(e, 4)/64. - 5 * Math.pow(e, 6)/256.) * Math.toRadians(phi)
                - ( 3 * Math.pow(e, 2)/8. + 3* Math.pow(e, 4)/32. + 45. * Math.pow(e, 6)/1024.) * Math.sin(2. * Math.toRadians(phi))
                + (15. * Math.pow(e, 4)/256. + 45. * Math.pow(e, 6)/1024.) * Math.sin(4 * Math.toRadians(phi))
                - (35. * Math.pow(e, 6)/3072.) * Math.sin(6. * Math.toRadians(phi)));
        return g;
    }

    public double[] calcNorthEast(double phi, double lambda)
    {
        double capA = (Math.toRadians(lambda - lambda0(lambda))) * Math.cos(Math.toRadians(phi));
        double capC = capE * Math.pow(Math.cos(Math.toRadians(phi)), 2);
        double capT = Math.pow(Math.tan(Math.toRadians(phi)), 2);
        double capN = a / Math.sqrt(1- Math.pow(e, 2) * Math.pow(Math.sin(Math.toRadians(phi)), 2));
        double g1 = 13 * Math.pow(capC, 2) + 4 * Math.pow(capC, 3) - 64 * Math.pow(capC, 2) * capT - 24 * Math.pow(capC, 3) * capT;
        double g2 = (61 - 479 * capT + 179 * Math.pow(capT, 2) - Math.pow(capT, 3)) * Math.pow(capA, 7) / 5040;
        double g3 = 445 * Math.pow(capC, 2) + 324 * Math.pow(capC, 3) - 680 * Math.pow(capC, 2) * capT + 884 * Math.pow(capC, 4) - 600 * Math.pow(capC, 3) * capT - 192 * Math.pow(capC, 4) * capT;
        double g4 = (1385 - 3111 * capT + 543 * Math.pow(capT, 2) - Math.pow(capT, 3)) * Math.pow(capA, 8) / 40320;

        double k = k0 * (1
                + (1 + capC) / 2  * Math.pow(capA, 2)
                + ( 5 - 4 * capT + 42 * capC + 13 * Math.pow(capC, 2) - 28 * capE - 48 * Math.pow(capC, 2) * capT + 4 * Math.pow(capC, 3) - 24 * Math.pow(capC, 3)* capT)* Math.pow(capA, 4) / 24
                + ( 61 - 148 * capT + 16 * Math.pow(capT, 2)) * Math.pow(capA, 6) / 720);
        double x = k0 * capN * (capA
                + (1 - capT + capC) * Math.pow(capA, 3)/6
                + (5 - 18 * capT + Math.pow(capT, 2) + 72 * capC - 58 * capE + g1) * Math.pow(capA, 5) / 120
                + g2);

        double y = k0 * (m(phi) + capN * Math.tan(Math.toRadians(phi))* (Math.pow(capA, 2) / 2
                + (5 - capT + 9 * capC + 4 * Math.pow(capC, 2)) * Math.pow(capA, 4) / 24
                + (61 - 58 * capT + Math.pow(capT, 2) + 600 * capC - 330 * capE + g3) * Math.pow(capA, 6) / 720) + g4);


        return new double[]{x + 500000.0, y , 30+ (3+ lambda0(lambda))/6};
    }

    public double lambda0(double lambda)
    {
        double lambda0 = 0.;
        if (lambda > 0 && lambda <= 6)
            lambda0 = 3.;
        else if(lambda > 6 && lambda <= 12)
            lambda0 = 9.;
        else if(lambda > 12 && lambda <= 18)
            lambda0 = 15.;
        return lambda0;
    }
}
