package org.overture.ide.plugins.latex.utility;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.overture.ast.lex.Dialect;
import org.overture.ast.util.definitions.ClassList;
import org.overture.ast.util.modules.ModuleList;
import org.overture.ide.core.resources.IVdmProject;
import org.overture.ide.core.resources.IVdmSourceUnit;
import org.overture.ide.plugins.latex.ILatexConstants;
import org.overture.ide.plugins.latex.LatexPlugin;
import org.overture.parser.lex.LexTokenReader;
import org.overture.parser.syntax.ClassReader;
import org.overture.parser.syntax.ModuleReader;

public class LatexUtilsBase
{

	public boolean hasGenerateMainDocument(IVdmProject project)
			throws CoreException
	{
		return project.getOptions().getGroup(LatexPlugin.PLUGIN_ID, true).getAttribute(ILatexConstants.LATEX_GENERATE_MAIN_DOCUMENT, true);
	}

	public String getDocument(IVdmProject project) throws CoreException
	{
		return project.getOptions().getGroup(LatexPlugin.PLUGIN_ID, true).getAttribute(ILatexConstants.LATEX_MAIN_DOCUMENT, "");
	}

	public boolean insertCoverageTable(IVdmProject project)
			throws CoreException
	{
		return project.getOptions().getGroup(LatexPlugin.PLUGIN_ID, true).getAttribute(ILatexConstants.LATEX_INCLUDE_COVERAGETABLE, true);
	}

	public boolean markCoverage(IVdmProject project) throws CoreException
	{
		return project.getOptions().getGroup(LatexPlugin.PLUGIN_ID, true).getAttribute(ILatexConstants.LATEX_MARK_COVERAGE, true);
	}

	public boolean modelOnly(IVdmProject project) throws CoreException
	{
		return project.getOptions().getGroup(LatexPlugin.PLUGIN_ID, true).getAttribute(ILatexConstants.LATEX_MODEL_ONLY, true);
	}

	public static String getFileName(File file)
	{
		int index = file.getName().lastIndexOf('.');
		return file.getName().substring(0, index);

	}

	static List<File> getFileChildern(File file)
	{
		List<File> list = new Vector<File>();

		if (file.isFile())
		{
			list.add(file);
			return list;
		}

		if (file != null && file.listFiles() != null)
		{
			for (File file2 : file.listFiles())
			{
				list.addAll(getFileChildern(file2));
			}
		}

		return list;

	}

	ClassList parseClasses(final IVdmProject project) throws CoreException
	{
		ClassReader reader;
		ClassList classes = new ClassList();
		for (IVdmSourceUnit source : project.getSpecFiles())
		{
			String charset = source.getFile().getCharset();

			LexTokenReader ltr = new LexTokenReader(source.getSystemFile(), Dialect.VDM_RT, charset);
			reader = new ClassReader(ltr);

			classes.addAll(reader.readClasses());
		}
		return classes;
	}

	ModuleList parseModules(final IVdmProject project) throws CoreException
	{
		ModuleReader reader;
		ModuleList modules = new ModuleList();
		for (IVdmSourceUnit source : project.getSpecFiles())
		{
			String charset = source.getFile().getCharset();

			LexTokenReader ltr = new LexTokenReader(source.getSystemFile(), Dialect.VDM_SL, charset);
			reader = new ModuleReader(ltr);

			modules.addAll(reader.readModules());

		}
		return modules;
	}

}
