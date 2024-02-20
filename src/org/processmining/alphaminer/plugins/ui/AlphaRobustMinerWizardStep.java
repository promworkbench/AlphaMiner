package org.processmining.alphaminer.plugins.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.framework.util.ui.widgets.BorderPanel;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.logabstractions.factories.DirectlyFollowsAbstractionFactory;

public class AlphaRobustMinerWizardStep extends ProMPropertiesPanel
		implements ProMWizardStep<AlphaRobustMinerParameters> {

	private static final long serialVersionUID = 7834404978722012351L;

	private static final String TITLE = "Configure Alpha Robust Miner parameters";

	private final BorderPanel causalThreshold;
	private final JLabel causalThresholdLabel;
	private final JSlider causalThresholdSlider;
	private final BorderPanel noiseThresholdLeastFreq;
	private final JLabel noiseThresholdLeastFreqLabel;
	private final JSlider noiseThresholdLeastFreqSlider;
	private final BorderPanel noiseThresholdMostFreq;
	private final JLabel noiseThresholdMostFreqLabel;
	private final JSlider noiseThresholdMostFreqSlider;

	private static final double DEFAULT_CAUSALTHRESHOLD = 5;
	private static final double DEFAULT_NOISETHRESHOLD_LEASTFREQ = 0.1;
	private static final double DEFAULT_NOISETHRESHOLD_MOSTFREQ = 0.01;
	private static final int SL_MIN = 0; // slider minimum value
	private static final int SL_MAX = Integer.MAX_VALUE; // slider maximum value

	private final double[][] dfr; // directly follow relation
	private final double[] ac; // activity count

	private final RobustParametersPanel dfrVisualization;

	public AlphaRobustMinerWizardStep(XLog log, XEventClassifier classifier) {
		super(TITLE);
		int h_min = 20, h_pref = 30, h_max = 40, w_min = 150, w_max = 1000, w_label = 80, w_slider = 400;
		
		causalThreshold = addProperty("Causality Threshold", new BorderPanel(15,3));
		causalThreshold.setLayout(new BorderLayout());
		causalThreshold.setBackground(WidgetColors.COLOR_LIST_BG);
		causalThreshold.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		causalThreshold.setPreferredSize(new Dimension(w_max,h_pref));
		causalThreshold.setMinimumSize(new Dimension(w_min,h_min));
		causalThreshold.setMaximumSize(new Dimension(w_max,h_max));
		causalThresholdSlider = new JSlider(SL_MIN, SL_MAX-1, getSliderFromCausal(DEFAULT_CAUSALTHRESHOLD));
		causalThresholdSlider.addChangeListener(new SliderListener());
		causalThresholdSlider.setLayout(new BorderLayout());
		causalThresholdSlider.setOpaque(false);
		causalThresholdSlider.setPreferredSize(new Dimension(w_slider,h_pref));
		causalThresholdSlider.setMinimumSize(new Dimension(w_min-w_label,h_min));
		causalThresholdSlider.setMaximumSize(new Dimension(w_slider,h_max));
		causalThresholdLabel = new JLabel(formatCausalLabel(getCausalFromSlider(causalThresholdSlider.getValue())));
		causalThresholdLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		causalThresholdLabel.setPreferredSize(new Dimension(w_label,h_pref));
		causalThresholdLabel.setMinimumSize(new Dimension(w_label,h_min));
		causalThresholdLabel.setMaximumSize(new Dimension(w_label,h_max));
		causalThreshold.add(causalThresholdLabel, BorderLayout.WEST);
		causalThreshold.add(causalThresholdSlider, BorderLayout.EAST);
		
		noiseThresholdLeastFreq = addProperty("Noise Threshold (LF)", new BorderPanel(15,3));
		noiseThresholdLeastFreq.setLayout(new BorderLayout());
		noiseThresholdLeastFreq.setBackground(WidgetColors.COLOR_LIST_BG);
		noiseThresholdLeastFreq.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		noiseThresholdLeastFreq.setPreferredSize(new Dimension(w_max,h_pref));
		noiseThresholdLeastFreq.setMinimumSize(new Dimension(w_min,h_min));
		noiseThresholdLeastFreq.setMaximumSize(new Dimension(w_max,h_max));
		noiseThresholdLeastFreqSlider = new JSlider(SL_MIN, SL_MAX-1, getSliderFromNoise(DEFAULT_NOISETHRESHOLD_LEASTFREQ));
		noiseThresholdLeastFreqSlider.addChangeListener(new SliderListener());
		noiseThresholdLeastFreqSlider.setLayout(new BorderLayout());
		noiseThresholdLeastFreqSlider.setOpaque(false);
		noiseThresholdLeastFreqSlider.setPreferredSize(new Dimension(w_slider,h_pref));
		noiseThresholdLeastFreqSlider.setMinimumSize(new Dimension(w_min-w_label,h_min));
		noiseThresholdLeastFreqSlider.setMaximumSize(new Dimension(w_slider,h_max));
		noiseThresholdLeastFreqLabel = new JLabel(formatNoiseLabel(getNoiseFromSlider(noiseThresholdLeastFreqSlider.getValue())));
		noiseThresholdLeastFreqLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		noiseThresholdLeastFreqLabel.setPreferredSize(new Dimension(w_label,h_pref));
		noiseThresholdLeastFreqLabel.setMinimumSize(new Dimension(w_label,h_min));
		noiseThresholdLeastFreqLabel.setMaximumSize(new Dimension(w_label,h_max));
		noiseThresholdLeastFreq.add(noiseThresholdLeastFreqLabel, BorderLayout.WEST);
		noiseThresholdLeastFreq.add(noiseThresholdLeastFreqSlider, BorderLayout.EAST);
		
		noiseThresholdMostFreq = addProperty("Noise Threshold (MF)", new BorderPanel(15,3));
		noiseThresholdMostFreq.setLayout(new BorderLayout());
		noiseThresholdMostFreq.setBackground(WidgetColors.COLOR_LIST_BG);
		noiseThresholdMostFreq.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		noiseThresholdMostFreq.setPreferredSize(new Dimension(w_max,h_pref));
		noiseThresholdMostFreq.setMinimumSize(new Dimension(w_min,h_min));
		noiseThresholdMostFreq.setMaximumSize(new Dimension(w_max,h_max));
		noiseThresholdMostFreqSlider = new JSlider(SL_MIN, SL_MAX-1, getSliderFromNoise(DEFAULT_NOISETHRESHOLD_MOSTFREQ));
		noiseThresholdMostFreqSlider.addChangeListener(new SliderListener());
		noiseThresholdMostFreqSlider.setLayout(new BorderLayout());
		noiseThresholdMostFreqSlider.setOpaque(false);
		noiseThresholdMostFreqSlider.setPreferredSize(new Dimension(w_slider,h_pref));
		noiseThresholdMostFreqSlider.setMinimumSize(new Dimension(w_min-w_label,h_min));
		noiseThresholdMostFreqSlider.setMaximumSize(new Dimension(w_slider,h_max));
		noiseThresholdMostFreqLabel = new JLabel(formatNoiseLabel(getNoiseFromSlider(noiseThresholdMostFreqSlider.getValue())));
		noiseThresholdMostFreqLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		noiseThresholdMostFreqLabel.setPreferredSize(new Dimension(w_label,h_pref));
		noiseThresholdMostFreqLabel.setMinimumSize(new Dimension(w_label,h_min));
		noiseThresholdMostFreqLabel.setMaximumSize(new Dimension(w_label,h_max));
		noiseThresholdMostFreq.add(noiseThresholdMostFreqLabel, BorderLayout.WEST);
		noiseThresholdMostFreq.add(noiseThresholdMostFreqSlider, BorderLayout.EAST);
		
//		noiseThresholdLeastFreq = addTextField("Noise Threshold on least frequent event ([0,1>)",
//				Double.toString(DEFAULT_NOISETHRESHOLD_LEASTFREQ));
//		noiseThresholdMostFreq = addTextField("Noise Threshold on most frequent event ([0,1>)",
//				Double.toString(DEFAULT_NOISETHRESHOLD_MOSTFREQ));
		
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		dfr = new double[classes.size()][classes.size()];
		ac = new double[classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				for (int i = 0; i < trace.size() - 1; i++) {
					XEventClass from = classes.getClassOf(trace.get(i));
					XEventClass to = classes.getClassOf(trace.get(i + 1));
					dfr[from.getIndex()][to.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					ac[from.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					if (i == trace.size() - 2) { // count final activity as well
						ac[to.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
			}
		}
		dfrVisualization = addProperty("Directly Follows Relations",
				new RobustParametersPanel(dfr, ac, getCausalThreshold(),
						getNoiseThresholdLeastFreq(), getNoiseThresholdMostFreq()));
	}

	public AlphaRobustMinerParameters apply(AlphaRobustMinerParameters model, JComponent component) {
		if (canApply(model, component)) {
			AlphaRobustMinerWizardStep step = (AlphaRobustMinerWizardStep) component;
			model.setCausalThreshold(step.getCausalThreshold());
			model.setNoiseThresholdLeastFreq(step.getNoiseThresholdLeastFreq());
			model.setNoiseThresholdMostFreq(step.getNoiseThresholdMostFreq());
		}
		return model;
	}

	public boolean canApply(AlphaRobustMinerParameters model, JComponent component) {
		return component instanceof AlphaRobustMinerWizardStep;
	}

	public JComponent getComponent(AlphaRobustMinerParameters model) {
		return this;
	}

	public String getTitle() {
		return TITLE;
	}

	public double getCausalThreshold() {
		return getCausalFromSlider(causalThresholdSlider.getValue());
	}

	public double getNoiseThresholdLeastFreq() {
		return getNoiseFromSlider(noiseThresholdLeastFreqSlider.getValue());
	}

	public double getNoiseThresholdMostFreq() {
		return getNoiseFromSlider(noiseThresholdMostFreqSlider.getValue());
	}

	private double getCausalFromSlider(int sliderValue) {
		return (SL_MAX - SL_MIN) / (double) (SL_MAX - sliderValue);
	}

	private int getSliderFromCausal(double causal) {
		return SL_MAX - (int) Math.round((SL_MAX - SL_MIN) / causal);
	}
	
	private double getNoiseFromSlider(int sliderValue) {
		return Math.pow((sliderValue - SL_MIN) / (double) (SL_MAX - SL_MIN),2);
	}
	
	private int getSliderFromNoise(double noise) {
		return SL_MIN + (int) Math.round(Math.sqrt(noise) * (SL_MAX - SL_MIN));
	}

	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			if (source.equals(causalThresholdSlider)) {
				causalThresholdLabel.setText(formatCausalLabel(getCausalFromSlider(causalThresholdSlider.getValue())));
				dfrVisualization.updateCausalThreshold(getCausalFromSlider(causalThresholdSlider.getValue()));
				dfrVisualization.repaint();
			}
			if (source.equals(noiseThresholdLeastFreqSlider)) {
				noiseThresholdLeastFreqLabel.setText(formatNoiseLabel(getNoiseFromSlider(noiseThresholdLeastFreqSlider.getValue())));
				dfrVisualization.updateNoiseThresholdLeastFreq(getNoiseFromSlider(noiseThresholdLeastFreqSlider.getValue()));
				dfrVisualization.repaint();
			}
			if (source.equals(noiseThresholdMostFreqSlider)) {
				noiseThresholdMostFreqLabel.setText(formatNoiseLabel(getNoiseFromSlider(noiseThresholdMostFreqSlider.getValue())));
				dfrVisualization.updateNoiseThresholdMostFreq(getNoiseFromSlider(noiseThresholdMostFreqSlider.getValue()));
				dfrVisualization.repaint();
			}
		}
	}
	
	static String formatCausalLabel(double n) {
	    if(Double.isInfinite(n) || Double.isNaN(n))
	        return Double.toString(n);
	    String result = String.format("%.3f", n);
	    if(result.length() > 7)
	        result = String.format("%.3e", n);
	    return result;
	}
	
	static String formatNoiseLabel(double n) {
		return String.format("%.4f", n);
	}
}
