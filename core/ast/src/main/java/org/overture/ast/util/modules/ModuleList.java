package org.overture.ast.util.modules;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.overture.ast.definitions.ANamedTraceDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexIdentifierToken;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexIdentifierToken;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.modules.AAllImport;
import org.overture.ast.modules.AFromModuleImports;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.modules.PImport;
import org.overture.ast.util.Utils;
import org.overture.config.Release;
import org.overture.config.Settings;

@SuppressWarnings("serial")
public class ModuleList extends Vector<AModuleModules>
{
	public ModuleList()
	{
		// empty
	}

	public ModuleList(List<AModuleModules> modules)
	{
		addAll(modules);
	}

	@Override
	public String toString()
	{
		return Utils.listToString(this);
	}

	public Set<File> getSourceFiles()
	{ 
		Set<File> files = new HashSet<File>();

		for (AModuleModules def : this)
		{
			files.addAll(def.getFiles());
		}

		return files;
	}

	public AModuleModules findModule(LexIdentifierToken sought)
	{
		for (AModuleModules m : this)
		{
			if (m.getName().equals(sought))
			{
				return m;
			}
		}

		return null;
	}

	public int combineDefaults()
	{
		int rv = 0;

		if (!isEmpty())
		{
			CombinedDefaultModule def = new CombinedDefaultModule(getFlatModules());
			
			if (Settings.release == Release.VDM_10)
			{
				// In VDM-10, we implicitly import all from the other
				// modules included with the flat specifications (if any).

				List<AFromModuleImports> imports = new Vector<AFromModuleImports>();

				for (AModuleModules m : this)
				{
					if (!m.getIsFlat())
					{
						imports.add(importAll(m.getName().clone()));
					}
				}

				if (!imports.isEmpty())
				{
					def.setImports(AstFactory.newAModuleImports(def.getName().clone(), imports));
				}
			}

			if (!def.getModules().isEmpty())
			{
				removeAll(getFlatModules());
				add(def);
			}
		}

		return rv;
	}

	private Set<AModuleModules> getFlatModules()
	{
		Set<AModuleModules> flats = new HashSet<AModuleModules>();

		for (AModuleModules m : this)
		{
			if (m.getIsFlat() && !(m instanceof CombinedDefaultModule))
			{
				flats.add(m);
			}
		}

		return flats;
	}

	public ANamedTraceDefinition findTraceDefinition(LexNameToken name)
	{
		for (AModuleModules m : this)
		{
			for (PDefinition d : m.getDefs())
			{
				if (name.equals(d.getName()))
				{
					if (d instanceof ANamedTraceDefinition)
					{
						return (ANamedTraceDefinition) d;
					} else
					{
						return null;
					}
				}
			}
		}

		return null;
	}

	// This function is copied from the module reader
	private AFromModuleImports importAll(ILexIdentifierToken from)
	{
		List<List<PImport>> types = new Vector<List<PImport>>();
		ILexNameToken all = new LexNameToken(from.getName(), "all", from.getLocation());
		List<PImport> impAll = new Vector<PImport>();
		AAllImport iport = AstFactory.newAAllImport(all);
		iport.setLocation(all.getLocation());
		iport.setName(all);
		iport.setRenamed(all);
		iport.setFrom(null);
		impAll.add(iport);
		types.add(impAll);
		return AstFactory.newAFromModuleImports(from, types);
	}
}
