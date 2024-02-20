package org.processmining.alphaminer.plugins.ui;

import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.alphaminer.parameters.DefaultEventClassifier;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

public class AlphaMinerWizardStep extends ProMPropertiesPanel implements ProMWizardStep<AlphaMinerParameters> {

	private static final long serialVersionUID = 7043633275971617176L;

	private static final String TITLE = "Configure Alpha Miner";

	private final ProMComboBox<XEventClassifier> classifierList;

	private final ProMComboBox<AlphaVersion> versionList;

	public AlphaMinerWizardStep(XLog log) {
		super(TITLE);
		List<XEventClassifier> classifiers = log.getClassifiers();
		if (classifiers.isEmpty()) {
			classifiers.add(DefaultEventClassifier.get());
		}
		classifierList = addComboBox("Event Classifier", classifiers);
		if (Boot.PLUGIN_LEVEL_THRESHOLD.getValue() >= PluginLevel.PeerReviewed.getValue()) {
			// Include only peer-reviewed versions of the Alpha Miner.
			versionList = addComboBox("Version",
					EnumSet.of(AlphaVersion.CLASSIC, AlphaVersion.PLUS, AlphaVersion.PLUS_PLUS, AlphaVersion.SHARP));
		} else {
			// Exclude those versions of the Alpha Mienr that are not peer-reviewed.
			versionList = addComboBox("Version", EnumSet.of(AlphaVersion.CLASSIC, AlphaVersion.PLUS,
					AlphaVersion.PLUS_PLUS, AlphaVersion.SHARP, AlphaVersion.ROBUST));
		}
	}

	public AlphaMinerParameters apply(AlphaMinerParameters model, JComponent component) {
		if (canApply(model, component)) {
			AlphaMinerWizardStep step = (AlphaMinerWizardStep) component;
			model.setVersion(step.getVersion());
		}
		return model;
	}

	public boolean canApply(AlphaMinerParameters model, JComponent component) {
		return component instanceof AlphaMinerWizardStep;
	}

	public JComponent getComponent(AlphaMinerParameters model) {
		return this;
	}

	public XEventClassifier getEventClassifier() {
		return (XEventClassifier) classifierList.getSelectedItem();
	}

	public String getTitle() {
		return TITLE;
	}

	public AlphaVersion getVersion() {
		return (AlphaVersion) versionList.getSelectedItem();
	}
}
