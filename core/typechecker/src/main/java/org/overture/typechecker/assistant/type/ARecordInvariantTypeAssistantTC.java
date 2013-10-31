package org.overture.typechecker.assistant.type;

import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.types.AAccessSpecifierAccessSpecifier;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.definition.PAccessSpecifierAssistantTC;



public class ARecordInvariantTypeAssistantTC {
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public ARecordInvariantTypeAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}
	public static AFieldField findField(ARecordInvariantType rec, String tag) {
		for (AFieldField f: rec.getFields())
		{
			if (f.getTag().equals(tag))
			{
				return f;
			}
		}

		return null;
	}

	public static void unResolve(ARecordInvariantType type) {
		if (!type.getResolved()) return; else { type.setResolved(false); }

		for (AFieldField f: type.getFields())
		{
			AFieldFieldAssistantTC.unResolve(f);
		}
		
	}

	public static PType typeResolve(ARecordInvariantType type,
			ATypeDefinition root,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) {
		
		if (type.getResolved())
		{
			return type;
		}
		else
		{
			type.setResolved(true);
			type.setInfinite(false);
		}

		for (AFieldField f: type.getFields())
		{
			if (root != null)
				root.setInfinite(false);

			AFieldFieldAssistantTC.typeResolve(f, root, rootVisitor, question);

			if (root != null)
				type.setInfinite(type.getInfinite() || root.getInfinite());
		}

		if (root != null) root.setInfinite(type.getInfinite());
		return type;
	}

	public static String toDisplay(ARecordInvariantType exptype) {
		return exptype.getName().toString();
	}

	public static PType isType(ARecordInvariantType exptype, String typename) {
		if (exptype.getOpaque()) return null;

		if (typename.indexOf('`') > 0)
		{
			return (exptype.getName().getFullName().equals(typename)) ? exptype : null;
		}
		else
		{
			// Local typenames aren't qualified with the local module name
			return (exptype.getName().getName().equals(typename)) ? exptype : null;
		}
	}

	public static boolean equals(ARecordInvariantType type, Object other) {
		other = PTypeAssistantTC.deBracket(other);

		if (other instanceof ARecordInvariantType)
		{
			ARecordInvariantType rother = (ARecordInvariantType)other;
			return type.getName().equals(rother.getName());	// NB. identical
		}

		return false;
	}

	public static boolean isRecord(ARecordInvariantType type) {
		if (type.getOpaque()) return false;
		return true;
	}
	
	public static ARecordInvariantType getRecord(ARecordInvariantType type) {		
		return type;
	}

	public static boolean narrowerThan(ARecordInvariantType type,
			AAccessSpecifierAccessSpecifier accessSpecifier) {		
		
		if (type.getInNarrower())
		{
			return false;
		}
		else
		{
			type.setInNarrower(true);
		}
		
		boolean result = false;
		
		if(type.getDefinitions().size() > 0)
		{
			for (PDefinition d: type.getDefinitions())
			{
				if (PAccessSpecifierAssistantTC.narrowerThan(d.getAccess(), accessSpecifier))
				{
					result = true;
					break;
				}
			}
		}
		else
		{
			for (AFieldField field : type.getFields())
			{
				if (PTypeAssistantTC.narrowerThan(field.getType(), accessSpecifier))
				{
					result = true;
					break;
				}
			}
		}
		
		type.setInNarrower(false);
		return result;
	}
}
