package org.overture.typechecker.assistant.definition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.ABusClassDefinition;
import org.overture.ast.definitions.AClassInvariantDefinition;
import org.overture.ast.definitions.ACpuClassDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AInheritedDefinition;
import org.overture.ast.definitions.APerSyncDefinition;
import org.overture.ast.definitions.ASystemClassDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.statements.PStm;
import org.overture.ast.typechecker.ClassDefinitionSettings;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.typechecker.Pass;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.PType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.FlatEnvironment;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.type.AClassTypeAssistantTC;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;
import org.overture.typechecker.util.HelpLexNameToken;
import org.overture.typechecker.visitor.TypeCheckVisitor;

public class SClassDefinitionAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public SClassDefinitionAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}
	//FIXME: Can't delete it is used in other places!
	public static PDefinition findName(SClassDefinition classdef,
			ILexNameToken sought, NameScope scope)
	{

		PDefinition def = null;

		for (PDefinition d : classdef.getDefinitions())
		{
			PDefinition found = PDefinitionAssistantTC.findName(d, sought, scope);

			// It is possible to have an ambiguous name if the name has
			// type qualifiers that are a union of types that match several
			// overloaded functions/ops (even though they themselves are
			// distinguishable).

			if (found != null)
			{
				if (def == null)
				{
					def = found;

					if (sought.getTypeQualifier() == null)
					{
						break; // Can't be ambiguous
					}
				} else
				{
					if (!def.getLocation().equals(found.getLocation())
							&& PDefinitionAssistantTC.isFunctionOrOperation(def))
					{
						TypeCheckerErrors.report(3010, "Name " + sought
								+ " is ambiguous", sought.getLocation(), sought);
						TypeCheckerErrors.detail2("1", def.getLocation(), "2", found.getLocation());
						break;
					}
				}
			}
		}

		if (def == null)
		{
			for (PDefinition d : classdef.getAllInheritedDefinitions())
			{
				PDefinition indef = PDefinitionAssistantTC.findName(d, sought, scope);

				// See above for the following...

				if (indef != null)
				{
					if (def == null)
					{
						def = indef;

						if (sought.getTypeQualifier() == null)
						{
							break; // Can't be ambiguous
						}
					} else if (def.equals(indef)
							&& // Compares qualified names
							!def.getLocation().equals(indef.getLocation())
							&& !hasSupertype(def.getClassDefinition(), indef.getClassDefinition().getType())
							&& PDefinitionAssistantTC.isFunctionOrOperation(def))
					{
						TypeCheckerErrors.report(3011, "Name " + sought
								+ " is multiply defined in class", sought.getLocation(), sought);
						TypeCheckerErrors.detail2("1", def.getLocation(), "2", indef.getLocation());
						break;
					}
				}
			}
		}

		return def;
	}

	public static boolean hasSupertype(SClassDefinition classDefinition,
			PType other)
	{

		if (PTypeAssistantTC.equals(getType(classDefinition), other))
		{
			return true;
		} else
		{
			for (PType type : classDefinition.getSupertypes())
			{
				AClassType sclass = (AClassType) type;

				if (AClassTypeAssistantTC.hasSupertype(sclass, other))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isAccessible(Environment env, PDefinition field,
			boolean needStatic)
	{
		SClassDefinition self = env.findClassDefinition();
		SClassDefinition target = field.getClassDefinition();

		if (self == null) // Not called from within a class member
		{
			// We're outside, so just public access
			return (PAccessSpecifierAssistantTC.isPublic(field.getAccess()));
		} else
		{
			AClassType selftype = (AClassType) getType(self);
			AClassType targtype = (AClassType) getType(target);

			if (!PTypeAssistantTC.equals(selftype, targtype))
			{
				if (AClassTypeAssistantTC.hasSupertype(selftype, targtype))
				{
					// We're a subclass, so see public or protected
					return (!PAccessSpecifierAssistantTC.isPrivate(field.getAccess()));
				} else
				{
					// We're outside, so just public/static access
					return (PAccessSpecifierAssistantTC.isPublic(field.getAccess()) && (needStatic ? PAccessSpecifierAssistantTC.isStatic(field.getAccess())
							: true));
				}
			} else
			{
				// else same type, so anything goes
				return true;
			}
		}
	}

	public static PDefinition findType(SClassDefinition classdef,
			ILexNameToken sought, String fromModule)
	{
		//FIXME: This method is used and outside the TypeFinder visitor so I can't delete it!
		//It is used in this class "public class PrivateClassEnvironment"
		//How do I proceed in this case?
		if ((!sought.getExplicit() && sought.getName().equals(classdef.getName().getName()))
				|| sought.equals(classdef.getName().getClassName()))
		{
			return classdef; // Class referred to as "A" or "CLASS`A"
		}

		PDefinition def = PDefinitionAssistantTC.findType(classdef.getDefinitions(), sought, null);

		if (def == null)
		{
			for (PDefinition d : classdef.getAllInheritedDefinitions())
			{
				PDefinition indef = PDefinitionAssistantTC.findType(d, sought, null);

				if (indef != null)
				{
					def = indef;
					break;
				}
			}
		}

		return def;
	}

	public static Set<PDefinition> findMatches(SClassDefinition classdef,
			ILexNameToken sought)
	{

		Set<PDefinition> set = PDefinitionListAssistantTC.findMatches(classdef.getDefinitions(), sought);
		set.addAll(PDefinitionListAssistantTC.findMatches(classdef.getAllInheritedDefinitions(), sought));
		return set;
	}

	public static PDefinition findName(List<SClassDefinition> classes,
			ILexNameToken name, NameScope scope)
	{

		SClassDefinition d = get(classes, name.getModule());
		if (d != null)
		{
			PDefinition def = SClassDefinitionAssistantTC.findName(d, name, scope);

			if (def != null)
			{
				return def;
			}
		}

		return null;
	}

	private static SClassDefinition get(List<SClassDefinition> classes,
			String module)
	{

		for (SClassDefinition sClassDefinition : classes)
		{
			if (sClassDefinition.getName().getName().equals(module))
				return sClassDefinition;
		}
		return null;
	}

	public static PDefinition findType(List<SClassDefinition> classes,
			ILexNameToken name)
	{

		for (SClassDefinition d : classes)
		{
			PDefinition def = PDefinitionAssistantTC.findType(d, name, null);

			if (def != null)
			{
				return def;
			}
		}

		return null;
	}

	public static Set<PDefinition> findMatches(List<SClassDefinition> classes,
			ILexNameToken name)
	{

		Set<PDefinition> set = new HashSet<PDefinition>();

		for (SClassDefinition d : classes)
		{
			set.addAll(SClassDefinitionAssistantTC.findMatches(d, name));
		}

		return set;
	}

	public static void unusedCheck(List<SClassDefinition> classes)
	{
		for (SClassDefinition d : classes)
		{
			PDefinitionAssistantTC.unusedCheck(d);
		}

	}

	public static List<PDefinition> getLocalDefinitions(
			SClassDefinition classDefinition)
	{

		List<PDefinition> all = new Vector<PDefinition>();

		all.addAll(classDefinition.getLocalInheritedDefinitions());
		all.addAll(PDefinitionListAssistantTC.singleDefinitions(classDefinition.getDefinitions()));

		return all;
	}

	public static PDefinition getSelfDefinition(SClassDefinition classDefinition)
	{

		PDefinition def = AstFactory.newALocalDefinition(classDefinition.getLocation(), classDefinition.getName().getSelfName(), NameScope.LOCAL, af.createPDefinitionAssistant().getType(classDefinition));
		PDefinitionAssistantTC.markUsed(def);
		return def;
	}

	public static void implicitDefinitions(SClassDefinition d,
			Environment publicClasses)
	{
		if (d instanceof ASystemClassDefinition)
		{
			ASystemClassDefinitionAssistantTC.implicitDefinitions((ASystemClassDefinition) d, publicClasses);
		} else
		{
			implicitDefinitionsBase(d, publicClasses);
		}

	}

	public static void implicitDefinitionsBase(SClassDefinition d,
			Environment publicClasses)
	{
		setInherited(d, publicClasses);
		setInheritedDefinitions(d);

		AExplicitOperationDefinition invariant = getInvDefinition(d);

		d.setInvariant(invariant);

		if (invariant != null)
		{
			PDefinitionAssistantTC.setClassDefinition(invariant, d);
		}

	}

	private static AExplicitOperationDefinition getInvDefinition(
			SClassDefinition d)
	{

		List<PDefinition> invdefs = getInvDefs(d);

		if (invdefs.isEmpty())
		{
			return null;
		}

		// Location of last local invariant
		ILexLocation invloc = invdefs.get(invdefs.size() - 1).getLocation();

		AOperationType type = AstFactory.newAOperationType(invloc, new Vector<PType>(), AstFactory.newABooleanBasicType(invloc));

		LexNameToken invname = new LexNameToken(d.getName().getName(), "inv_"
				+ d.getName().getName(), invloc);

		PStm body = AstFactory.newAClassInvariantStm(invname, invdefs);

		return AstFactory.newAExplicitOperationDefinition(invname, type, new Vector<PPattern>(), null, null, body);
	}

	public static List<PDefinition> getInvDefs(SClassDefinition def)
	{
		List<PDefinition> invdefs = new Vector<PDefinition>();

		if (def.getGettingInvDefs())
		{
			// reported elsewhere
			return invdefs;
		}

		def.setGettingInvDefs(true);

		for (SClassDefinition d : def.getSuperDefs())
		{
			invdefs.addAll(getInvDefs(d));
		}

		for (PDefinition d : def.getDefinitions())
		{
			if (d instanceof AClassInvariantDefinition)
			{
				invdefs.add(d);
			}
		}

		def.setGettingInvDefs(false);
		return invdefs;
	}

	private static void setInheritedDefinitions(SClassDefinition definition)
	{
		List<PDefinition> indefs = new Vector<PDefinition>();

		for (SClassDefinition sclass : definition.getSuperDefs())
		{
			indefs.addAll(getInheritable(sclass));
		}

		// The inherited definitions are ordered such that the
		// definitions, taken in order, will consider the overriding
		// members before others.

		List<PDefinition> superInheritedDefinitions = new Vector<PDefinition>();

		for (PDefinition d : indefs)
		{
			superInheritedDefinitions.add(d);

			ILexNameToken localname = d.getName().getModifiedName(definition.getName().getName());

			if (PDefinitionListAssistantTC.findName(definition.getDefinitions(), localname, NameScope.NAMESANDSTATE) == null)
			{
				AInheritedDefinition local = AstFactory.newAInheritedDefinition(localname, d);
				definition.getLocalInheritedDefinitions().add(local);
			}
		}

		definition.setSuperInheritedDefinitions(superInheritedDefinitions);
		definition.setAllInheritedDefinitions(new Vector<PDefinition>());
		definition.getAllInheritedDefinitions().addAll(superInheritedDefinitions);
		definition.getAllInheritedDefinitions().addAll(definition.getLocalInheritedDefinitions());

	}

	private static List<PDefinition> getInheritable(SClassDefinition def)
	{

		List<PDefinition> defs = new Vector<PDefinition>();

		if (def.getGettingInheritable())
		{
			TypeCheckerErrors.report(3009, "Circular class hierarchy detected: "
					+ def.getName(), def.getLocation(), def);
			return defs;
		}

		def.setGettingInheritable(true);

		// The inherited definitions are ordered such that the
		// definitions, taken in order, will consider the overriding
		// members before others. So we add the local definitions
		// before the inherited ones.

		List<PDefinition> singles = PDefinitionListAssistantTC.singleDefinitions(def.getDefinitions());

		for (PDefinition d : singles)
		{
			if (!PAccessSpecifierAssistantTC.isPrivate(d.getAccess()))
			{
				defs.add(d);
			}
		}

		for (SClassDefinition sclass : def.getSuperDefs())
		{
			List<PDefinition> sdefs = getInheritable(sclass);

			for (PDefinition d : sdefs)
			{
				defs.add(d);

				ILexNameToken localname = d.getName().getModifiedName(def.getName().getName());

				if (PDefinitionListAssistantTC.findName(defs, localname, NameScope.NAMESANDSTATE) == null)
				{
					AInheritedDefinition local = AstFactory.newAInheritedDefinition(localname, d);
					defs.add(local);
				}
			}
		}

		def.setGettingInheritable(false);
		return defs;
	}

	private static void setInherited(SClassDefinition d, Environment base)
	{
		switch (d.getSettingHierarchy())
		{
			case UNSET:
				d.setSettingHierarchy(ClassDefinitionSettings.INPROGRESS);
				break;

			case INPROGRESS:
				TypeCheckerErrors.report(3002, "Circular class hierarchy detected: "
						+ d.getName(), d.getLocation(), d);
				return;

			case DONE:
				return;
		}

		PDefinitionListAssistantTC.implicitDefinitions(d.getDefinitions(), base);

		for (ILexNameToken supername : d.getSupernames())
		{
			PDefinition def = base.findType(supername, null);

			if (def == null)
			{
				TypeCheckerErrors.report(3003, "Undefined superclass: "
						+ supername, d.getLocation(), d);
			} else if (def instanceof ACpuClassDefinition)
			{
				TypeCheckerErrors.report(3298, "Cannot inherit from CPU", d.getLocation(), d);
			} else if (def instanceof ABusClassDefinition)
			{
				TypeCheckerErrors.report(3299, "Cannot inherit from BUS", d.getLocation(), d);
			} else if (def instanceof ASystemClassDefinition)
			{
				TypeCheckerErrors.report(3278, "Cannot inherit from system class "
						+ supername, d.getLocation(), d);
			} else if (def instanceof SClassDefinition)
			{
				SClassDefinition superdef = (SClassDefinition) def;
				setInherited(superdef, base);

				d.getSuperDefs().add(superdef);
				d.getSupertypes().add(af.createPDefinitionAssistant().getType(superdef));
			} else
			{
				TypeCheckerErrors.report(3004, "Superclass name is not a class: "
						+ supername, d.getLocation(), d);
			}
		}

		d.setSettingHierarchy(ClassDefinitionSettings.DONE);
		return;

	}

	public static void typeResolve(SClassDefinition d,
			QuestionAnswerAdaptor<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) throws AnalysisException
	{

		Environment cenv = new FlatEnvironment(question.assistantFactory, d.getDefinitions(), question.env);
		PDefinitionListAssistantTC.typeResolve(d.getDefinitions(), rootVisitor, new TypeCheckInfo(question.assistantFactory, cenv));
	}

	public static PDefinition findThread(SClassDefinition d)
	{
		return SClassDefinitionAssistantTC.findName(d, d.getName().getThreadName(), NameScope.NAMES);
	}

	public static PDefinition findConstructor(SClassDefinition classdef,
			List<PType> argtypes)
	{

		LexNameToken constructor = getCtorName(classdef, argtypes);
		return findName(classdef, constructor, NameScope.NAMES);
	}

	public static LexNameToken getCtorName(SClassDefinition classdef,
			List<PType> argtypes)
	{
		ILexNameToken name = classdef.getName();
		LexNameToken cname = new LexNameToken(name.getName(), name.getName(), classdef.getLocation());
		cname.setTypeQualifier(argtypes);
		return cname;
	}

	public static PType getType(SClassDefinition def)
	{
		if (def.getClasstype() == null)
		{
			def.setClasstype(AstFactory.newAClassType(def.getLocation(), def));
		}

		return def.getClasstype();
	}

	public static void checkOver(SClassDefinition c)
	{
		int inheritedThreads = 0;
		checkOverloads(c);

		List<List<PDefinition>> superlist = new Vector<List<PDefinition>>();

		for (PDefinition def : c.getSuperDefs())
		{
			SClassDefinition superdef = (SClassDefinition) def;
			List<PDefinition> inheritable = SClassDefinitionAssistantTC.getInheritable(superdef);
			superlist.add(inheritable);

			if (checkOverrides(c, inheritable))
			{
				inheritedThreads++;
			}
		}

		if (inheritedThreads > 1)
		{
			TypeCheckerErrors.report(3001, "Class inherits thread definition from multiple supertypes", c.getLocation(), c);
		}

		checkAmbiguities(c, superlist);

	}

	private static void checkAmbiguities(SClassDefinition c,
			List<List<PDefinition>> superlist)
	{
		int count = superlist.size();

		for (int i = 0; i < count; i++)
		{
			List<PDefinition> defs = superlist.get(i);

			for (int j = i + 1; j < count; j++)
			{
				List<PDefinition> defs2 = superlist.get(j);
				checkAmbiguities(c, defs, defs2);
			}
		}

	}

	private static void checkAmbiguities(SClassDefinition c,
			List<PDefinition> defs, List<PDefinition> defs2)
	{

		for (PDefinition indef : defs)
		{
			ILexNameToken localName = indef.getName().getModifiedName(c.getName().getName());

			for (PDefinition indef2 : defs2)
			{
				if (!indef.getLocation().equals(indef2.getLocation())
						&& PDefinitionAssistantTC.kind(indef).equals(PDefinitionAssistantTC.kind(indef2)))
				{
					ILexNameToken localName2 = indef2.getName().getModifiedName(c.getName().getName());

					if (HelpLexNameToken.isEqual(localName, localName2))
					{
						PDefinition override = PDefinitionListAssistantTC.findName(c.getDefinitions(), localName, NameScope.NAMESANDSTATE);

						if (override == null) // OK if we override the ambiguity
						{
							TypeCheckerErrors.report(3276, "Ambiguous definitions inherited by "
									+ c.getName().getName(), c.getLocation(), c);
							TypeCheckerErrors.detail("1", indef.getName() + " "
									+ indef.getLocation());
							TypeCheckerErrors.detail("2", indef2.getName()
									+ " " + indef2.getLocation());
						}
					}
				}
			}
		}

	}

	private static boolean checkOverrides(SClassDefinition c,
			List<PDefinition> inheritable)
	{
		boolean inheritedThread = false;

		for (PDefinition indef : inheritable)
		{
			if (indef.getName().getName().equals("thread"))
			{
				inheritedThread = true;
				continue; // No other checks needed for threads
			}

			ILexNameToken localName = indef.getName().getModifiedName(c.getName().getName());

			PDefinition override = PDefinitionListAssistantTC.findName(c.getDefinitions(), localName, NameScope.NAMESANDSTATE);

			if (override == null)
			{
				override = PDefinitionListAssistantTC.findType(c.getDefinitions(), localName, null);
			}

			if (override != null)
			{
				if (!PDefinitionAssistantTC.kind(indef).equals(PDefinitionAssistantTC.kind(override)))
				{
					TypeCheckerErrors.report(3005, "Overriding a superclass member of a different kind: "
							+ override.getName(), override.getName().getLocation(), override);
					TypeCheckerErrors.detail2("This", PDefinitionAssistantTC.kind(override), "Super", PDefinitionAssistantTC.kind(indef));
				} else if (PAccessSpecifierAssistantTC.narrowerThan(override.getAccess(), indef.getAccess()))
				{
					TypeCheckerErrors.report(3006, "Overriding definition reduces visibility", override.getName().getLocation(), override);
					TypeCheckerErrors.detail2("This", override.getName(), "Super", indef.getName());
				} else
				{
					PType to = af.createPDefinitionAssistant().getType(indef);
					PType from = af.createPDefinitionAssistant().getType(override);

					// Note this uses the "parameters only" comparator option

					if (!TypeComparator.compatible(to, from, true))
					{
						TypeCheckerErrors.report(3007, "Overriding member incompatible type: "
								+ override.getName().getName(), override.getLocation(), override);
						TypeCheckerErrors.detail2("This", override.getType(), "Super", indef.getType());
					}
				}
			}
		}

		return inheritedThread;
	}

	private static void checkOverloads(SClassDefinition c)
	{
		List<String> done = new Vector<String>();

		List<PDefinition> singles = PDefinitionListAssistantTC.singleDefinitions(c.getDefinitions());

		for (PDefinition def1 : singles)
		{
			for (PDefinition def2 : singles)
			{
				if (def1 != def2
						&& def1.getName() != null
						&& def2.getName() != null
						&& def1.getName().getName().equals(def2.getName().getName())
						&& !done.contains(def1.getName().getName()))
				{
					if ((PDefinitionAssistantTC.isFunction(def1) && PDefinitionAssistantTC.isFunction(def2))
							|| (PDefinitionAssistantTC.isOperation(def1) && PDefinitionAssistantTC.isOperation(def2)))
					{
						PType to = def1.getType();
						PType from = def2.getType();

						// Note this uses the "parameters only" comparator option

						if (TypeComparator.compatible(to, from, true))
						{
							TypeCheckerErrors.report(3008, "Overloaded members indistinguishable: "
									+ def1.getName().getName(), def1.getLocation(), def1);
							TypeCheckerErrors.detail2(def1.getName().getName(), def1.getType(), def2.getName().getName(), def2.getType());
							done.add(def1.getName().getName());
						}
					} else
					{
						// Class invariants can duplicate if there are several
						// "inv" clauses in one class...

						if (!(def1 instanceof AClassInvariantDefinition)
								&& !(def2 instanceof AClassInvariantDefinition)
								&& !(def1 instanceof APerSyncDefinition)
								&& !(def2 instanceof APerSyncDefinition))
						{
							TypeCheckerErrors.report(3017, "Duplicate definitions for "
									+ def1.getName().getName(), def1.getName().getLocation(), def1);
							TypeCheckerErrors.detail2(def1.getName().getName(), def1.getLocation(), def2.getName().getName(), def2.getLocation());
							done.add(def1.getName().getName());
						}
					}
				}
			}
		}

	}

	public static void typeCheckPass(SClassDefinition c, Pass p,
			Environment base, QuestionAnswerAdaptor<TypeCheckInfo, PType> tc) throws AnalysisException
	{
		if (c.getTypeChecked())
			return;

		for (PDefinition d : c.getDefinitions())
		{
			if (d.getPass() == p)
			{
				d.apply(tc, new TypeCheckInfo(af, base, NameScope.NAMES));
			}
		}

		if (c.getInvariant() != null && c.getInvariant().getPass() == p)
		{
			c.getInvariant().apply(tc, new TypeCheckInfo(af, base, NameScope.NAMES));
		}

	}

	public static void initializedCheck(SClassDefinition c)
	{
		PDefinitionListAssistantTC.initializedCheck(c.getDefinitions());
	}

}
