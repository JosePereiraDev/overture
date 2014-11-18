/*
 * #%~
 * Code Generator Plugin
 * %%
 * Copyright (C) 2008 - 2014 Overture
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #~%
 */
package org.overture.ide.plugins.codegen.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.Preferences;
import org.overture.ide.plugins.codegen.Activator;
import org.overture.ide.plugins.codegen.ICodeGenConstants;
import org.overture.ide.plugins.codegen.util.PluginVdm2JavaUtil;

public class WorkbenchPreferencePageJavaCodeGen extends PreferencePage implements
		IWorkbenchPreferencePage
{
	private Button disableCloningCheckBox;
	private Button generateAsStrCheckBox;
	private Text classesToSkipField;

	@Override
	protected IPreferenceStore doGetPreferenceStore()
	{
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		disableCloningCheckBox = new Button(composite, SWT.CHECK);
		disableCloningCheckBox.setText("Disable cloning");

		generateAsStrCheckBox = new Button(composite, SWT.CHECK);
		generateAsStrCheckBox.setText("Generate character sequences as strings");

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		Label label = new Label(parent, SWT.NULL);
		label.setText("Classes that should not be code generated. Separate by ';' (e.g. World; Env)");
		classesToSkipField = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		classesToSkipField.setLayoutData(gridData);

		refreshControls();

		return composite;
	}

	@Override
	protected void performApply()
	{
		apply();
		super.performApply();
		
		PluginVdm2JavaUtil.getClassesToSkip();
	}
	
	@Override
	public boolean performOk()
	{
		apply();
		return super.performOk();
	}

	private void apply()
	{
		IPreferenceStore store = doGetPreferenceStore();
		
		boolean disableCloning = disableCloningCheckBox.getSelection();
		store.setDefault(ICodeGenConstants.DISABLE_CLONING, disableCloning);
		
		boolean generateAsStrings = generateAsStrCheckBox.getSelection();
		store.setDefault(ICodeGenConstants.GENERATE_CHAR_SEQUENCES_AS_STRINGS, generateAsStrings);
		
		String userSpecifiedClassesToSkip = classesToSkipField.getText();
		store.setDefault(ICodeGenConstants.CLASSES_TO_SKIP, userSpecifiedClassesToSkip);

		Activator.savePluginSettings(disableCloning, generateAsStrings, userSpecifiedClassesToSkip);;
		
		refreshControls();
	}

	@Override
	protected void performDefaults()
	{
		super.performDefaults();
		
		if(disableCloningCheckBox != null)
		{
			disableCloningCheckBox.setSelection(ICodeGenConstants.DISABLE_CLONING_DEFAULT);
		}
		
		if(generateAsStrCheckBox != null)
		{
			generateAsStrCheckBox.setSelection(ICodeGenConstants.GENERATE_CHAR_SEQUENCES_AS_STRING_DEFAULT);
		}
		
		if(classesToSkipField != null)
		{
			classesToSkipField.setText(ICodeGenConstants.CLASSES_TO_SKIP_DEFAULT);
		}
	}
	
	@Override
	public void init(IWorkbench workbench)
	{
		refreshControls();
	}

	private void refreshControls()
	{
		Preferences preferences = InstanceScope.INSTANCE.getNode(ICodeGenConstants.PLUGIN_ID);

		if (disableCloningCheckBox != null)
		{
			disableCloningCheckBox.setSelection(preferences.getBoolean(ICodeGenConstants.DISABLE_CLONING, ICodeGenConstants.DISABLE_CLONING_DEFAULT));
		}

		if (generateAsStrCheckBox != null)
		{
			generateAsStrCheckBox.setSelection(preferences.getBoolean(ICodeGenConstants.GENERATE_CHAR_SEQUENCES_AS_STRINGS, ICodeGenConstants.GENERATE_CHAR_SEQUENCES_AS_STRING_DEFAULT));
		}
		
		if (classesToSkipField != null)
		{
			classesToSkipField.setText(preferences.get(ICodeGenConstants.CLASSES_TO_SKIP, ICodeGenConstants.CLASSES_TO_SKIP_DEFAULT));
		}
	}

}