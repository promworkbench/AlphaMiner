package org.processmining.alphaminer.plugins.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class RobustParametersPanel extends JPanel {
	
	private static final long serialVersionUID = 3072946836158111347L;
	
	private final double[][] dfr; // directly follow relation
	private final double[] ac; // activity count
	private double causalThreshold;
	private double noiseThresholdLeastFreq;
	private double noiseThresholdMostFreq;
	
	public RobustParametersPanel(double[][] dfr, double[] ac, double causalThreshold,
			double noiseThresholdLeastFreq, double noiseThresholdMostFreq) {
		this.dfr = dfr;
		this.ac = ac;
		this.causalThreshold = causalThreshold;
		this.noiseThresholdLeastFreq = noiseThresholdLeastFreq;
		this.noiseThresholdMostFreq = noiseThresholdMostFreq;
	}
	
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        setPreferredSize(new Dimension(200,200));
        setMinimumSize(new Dimension(200,200));
        setMaximumSize(new Dimension(200,200));
        setSize(new Dimension(200,200));
        int w = getWidth();
        int h = getHeight();
        double radius = 2;
        double minmax;
        double realNoiseThreshold;
        for(int r = 0; r < dfr.length; r++) {
        	for (int c = 0; c < dfr[r].length; c++) {
        		g2.setPaint(Color.gray);
        		g2.draw(new Line2D.Double(0, h, w, 0));
        		if (dfr[r][c]>dfr[c][r]) {
        			minmax = Math.min(ac[r], ac[c]);
        		} else {
        			minmax = Math.max(ac[r], ac[c]);
        		}
        		realNoiseThreshold = Math.max(
						noiseThresholdLeastFreq * Math.min(ac[r], ac[c]),
						noiseThresholdMostFreq * Math.max(ac[r], ac[c])
						);
        		g2.setPaint(Color.red);
        		if (dfr[r][c] <= realNoiseThreshold && dfr[c][r] <= realNoiseThreshold) {
        			g2.setPaint(Color.gray);
        		} else if (dfr[r][c] >= 1 && dfr[c][r] >=1) {
        			if (dfr[r][c]/dfr[c][r] < causalThreshold && dfr[c][r]/dfr[r][c] < causalThreshold) {
        				g2.setPaint(Color.blue);
        			}
        		}
        		double x = w*dfr[r][c]/minmax;
                double y = h-(h*dfr[c][r]/minmax);
                g2.draw(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
        	}
        }
        g2.setPaint(Color.blue);
        g2.draw(new Line2D.Double(0, h, w, h-h/causalThreshold));
        g2.draw(new Line2D.Double(0, h, w/causalThreshold, 0));
        g2.setPaint(Color.green);
        g2.draw(new Line2D.Double(w*noiseThresholdLeastFreq, h,
        		w*noiseThresholdLeastFreq,h-h*noiseThresholdLeastFreq));
        g2.draw(new Line2D.Double(0, h,
        		w*noiseThresholdLeastFreq, h-h*noiseThresholdLeastFreq));
        g2.draw(new Line2D.Double(0, h-h*noiseThresholdMostFreq,
        		w*noiseThresholdMostFreq, h-h*noiseThresholdMostFreq));
        g2.draw(new Line2D.Double(0, h,
        		w*noiseThresholdMostFreq, h-h*noiseThresholdMostFreq));
    }
	
	public void updateCausalThreshold(double causalThreshold) {
		this.causalThreshold = causalThreshold;
	}
	
	public void updateNoiseThresholdLeastFreq(double noiseThreshold) {
		this.noiseThresholdLeastFreq = noiseThreshold;
	}
	
	public void updateNoiseThresholdMostFreq(double noiseThreshold) {
		this.noiseThresholdMostFreq = noiseThreshold;
	}
}
