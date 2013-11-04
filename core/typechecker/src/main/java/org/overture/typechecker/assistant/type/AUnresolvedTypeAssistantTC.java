package org.overture.typechecker.assistant.type;

import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.ABusClassDefinition;
import org.overture.ast.definitions.ACpuClassDefinition;
import org.overture.ast.definitions.AImportedDefinition;
import org.overture.ast.definitions.AInheritedDefinition;
import org.overture.ast.definitions.ARenamedDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.AUnresolvedType;
import org.overture.ast.types.PType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class AUnresolvedTypeAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public AUnresolvedTypeAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static PType typeResolve(AUnresolvedType type, ATypeDefinition root,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question)
	{

		PType deref = dereference(type, question.env, root);

		if (!(deref instanceof AClassType))
		{
			deref = af.createPTypeAssistant().typeResolve(deref, root, rootVisitor, question);
		}

		// TODO: return deref.clone()
		return deref;
	}

	private static PType dereference(AUnresolvedType type, Environment env,
			ATypeDefinition root)
	{
		PDefinition def = env.findType(type.getName(), type.getLocation().getModule());

		if (def == null)
		{
			throw new TypeCheckException("Unable to resolve type name '"
					+ type.getName() + "'", type.getLocation(), type);
		}

		if (def instanceof AImportedDefinition)
		{
			AImportedDefinition idef = (AImportedDefinition) def;
			def = idef.getDef();
		}

		if (def instanceof ARenamedDefinition)
		{
			ARenamedDefinition rdef = (ARenamedDefinition) def;
			def = rdef.getDef();
		}

		if (!(def instanceof ATypeDefinition)
				&& !(def instanceof AStateDefinition)
				&& !(def instanceof SClassDefinition)
				&& !(def instanceof AInheritedDefinition))
		{
			TypeCheckerErrors.report(3434, "'" + type.getName()
					+ "' is not the name of a type definition", type.getLocation(), type);
		}

		if (def instanceof ATypeDefinition)
		{
			if (def == root)
			{
				root.setInfinite(true);
			}
		}

		if ((def instanceof ACpuClassDefinition || def instanceof ABusClassDefinition)
				&& !env.isSystem())
		{
			TypeCheckerErrors.report(3296, "Cannot use '" + type.getName()
					+ "' outside system class", type.getLocation(), type);
		}

		PType r = null;
		// if(def instanceof ATypeDefinition)
		// {
		// r = ((ATypeDefinition)def).getInvType();
		// }
		// else if(def instanceof AStateDefinition)
		// {
		// r = ((AStateDefinition)def).getRecordType();
		// } else
		// {
		r = af.createPDefinitionAssistant().getType(def);
		// }

		List<PDefinition> tempDefs = new Vector<PDefinition>();
		tempDefs.add(def);
		r.setDefinitions(tempDefs);
		return r;
	}

	// public static String toDisplay(AUnresolvedType exptype) {
	// return "(unresolved " + exptype.getName().getExplicit(true) + ")";
	//
	// }

	public static PType isType(AUnresolvedType exptype, String typename)
	{
		return exptype.getName().getFullName().equals(typename) ? exptype
				: null;
	}

	// public static boolean equals(AUnresolvedType type, Object other) {
	// other = PTypeAssistantTC.deBracket(other);
	//
	// if (other instanceof AUnresolvedType)
	// {
	// AUnresolvedType nother = (AUnresolvedType)other;
	// return type.getName().equals(nother.getName());
	// }
	//
	// if (other instanceof ANamedInvariantType)
	// {
	// ANamedInvariantType nother = (ANamedInvariantType)other;
	// return type.getName().equals(nother.getName());
	// }
	//
	// return false;
	// }

}
