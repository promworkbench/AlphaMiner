package org.processmining.alphaminer.plugins.ui;

import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.parameters.DefaultEventClassifier;
import org.processmining.alphaminer.parameters.RealizablePlacesParameters;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

public class RealizablePlacesWizardStep extends ProMPropertiesPanel implements ProMWizardStep<RealizablePlacesParameters> {
	
	private static final long serialVersionUID = -8986746125916235600L;

	private static final String TITLE = "Configure Realizability Parameters";
	
	private static final double DEFAULT_REALIZABILITY = 0.1;
	private static final double DEFAULT_UNREALIZABLE_TRACES = 0.1;
	
	private final UIPluginContext context;
	
	private final ProMComboBox<XEventClassifier> classifierList;
	private final ProMTextField realizabilityThresholdField;
	private final ProMTextField unrealizableTracesThresholdField;
	
	public RealizablePlacesWizardStep(UIPluginContext context, XLog log) {
		super(TITLE);
		this.context = context;
		List<XEventClassifier> classifiers = log.getClassifiers();
		if (classifiers.isEmpty()) {
			classifiers.add(DefaultEventClassifier.get());
		}
		classifierList = addComboBox("Event Classifier", classifiers);
		realizabilityThresholdField = addTextField("Realizability Threshold",
				Double.toString(DEFAULT_REALIZABILITY));
		unrealizableTracesThresholdField = addTextField("Unrealizable Traces Threshold",
				Double.toString(DEFAULT_UNREALIZABLE_TRACES));
	}
	
	public RealizablePlacesParameters apply(RealizablePlacesParameters model, JComponent component) {
		if (canApply(model, component)) {
			RealizablePlacesWizardStep step = (RealizablePlacesWizardStep) component;
			model.setRealizabilityThreshold(step.getRealizabilityThreshold());
			model.setUnrealizableTracesThreshold(step.getUnrealizableTracesThreshold());
		}
		return model;
	}
	
	public boolean canApply(RealizablePlacesParameters model, JComponent component) {
		return component instanceof RealizablePlacesWizardStep;
	}
	
	public JComponent getComponent(RealizablePlacesParameters model) {
		return this;
	}
	
	public XEventClassifier getEventClassifier() {
		return (XEventClassifier) classifierList.getSelectedItem();
	}
	
	public double getRealizabilityThreshold() {
		try {
			double result = Double.parseDouble(realizabilityThresholdField.getText());
			if (result < 0){
				context.log("Input for Realizability Threshold cannot be negative, using 0 instead",
						MessageLevel.WARNING);
				result = 0;
			} else if (result > 1) {
				context.log("Input for Realizability Threshold must be smaller or equal to 1, using 1 instead",
						MessageLevel.WARNING);
				result = 1;
			}
			return result;
		}
		catch(NumberFormatException e) {
			context.log("Input for Realizability Threshold is not a number, using default: "
					+ DEFAULT_REALIZABILITY, MessageLevel.WARNING);
			return DEFAULT_REALIZABILITY;
		}
	}
	
	public double getUnrealizableTracesThreshold() {
		try {
			double result = Double.parseDouble(unrealizableTracesThresholdField.getText());
			if (result < 0){
				context.log("Input for Unrealizable Traces Threshold cannot be negative, using 0 instead",
						MessageLevel.WARNING);
				result = 0;
			} else if (result > 1) {
				context.log("Input for Unrealizable Traces Threshold must be smaller or equal to 1, using 1 instead",
						MessageLevel.WARNING);
				result = 1;
			}
			return result;
		}
		catch(NumberFormatException e) {
			context.log("Input for Unrealizable Traces Threshold is not a number, using default: "
					+ DEFAULT_UNREALIZABLE_TRACES, MessageLevel.WARNING);
			return DEFAULT_UNREALIZABLE_TRACES;
		}
	}
	
	public String getTitle() {
		return TITLE;
	}
}
