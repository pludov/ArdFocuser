package fr.pludov.ardfocuser.ui.util;

import java.awt.Color;

public class ColorContrast {
	
	private static double getLuminance(Color c)
	{
	    double r = Math.pow((c.getRed()/255.0), 2.2);
	    double b = Math.pow((c.getBlue()/255.0), 2.2);
	    double g = Math.pow((c.getGreen()/255.0), 2.2);

	    double y = 0.2126*r + 0.7151*g + 0.0721*b;
	    
	    return y;
	}
	
	private static double contrast(double b, double d)
	{
		if (b < d) {
			double tmp = b;
			b = d;
			d = tmp;
		}
		
		return (b + 0.05) / (d + 0.05);
	}
	
	public static Color getBestContrastFor(Color from)
	{
		double yfrom = getLuminance(from);
		double yBlack = getLuminance(Color.BLACK);
		double yWhite = getLuminance(Color.WHITE);
		
		double cblack = contrast(yBlack, yfrom);
		if (cblack > 4.5) {
			return Color.BLACK;
		}
		
		double cwhite = contrast(yWhite, yfrom);
		if (cwhite > 4.5 || cwhite > cblack) {
			return Color.WHITE;
		}
		
		return Color.BLACK;
		
	}

}
