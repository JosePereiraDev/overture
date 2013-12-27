package org.overture.typechecker.utilities.type;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.node.INode;
import org.overture.ast.types.AAccessSpecifierAccessSpecifier;
import org.overture.ast.types.ABracketType;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.AOptionalType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.AUnknownType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SInvariantType;
import org.overture.ast.types.SSeqType;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.definition.PAccessSpecifierAssistantTC;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;

/**
 * Checks if a type is smaller than a specifier type.
 * 
 * @author kel
 */
public class NarrowerThanComparator extends
		QuestionAnswerAdaptor<AAccessSpecifierAccessSpecifier, Boolean>
{

	protected ITypeCheckerAssistantFactory af;

	public NarrowerThanComparator(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	@Override
	public Boolean caseABracketType(ABracketType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		return type.getType().apply(this, accessSpecifier);
	}

	@Override
	public Boolean caseAFunctionType(AFunctionType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{

		for (PType t : type.getParameters())
		{
			if (t.apply(this, accessSpecifier)) // (PTypeAssistantTC.narrowerThan(t, accessSpecifier))
			{
				return true;
			}
		}

		return type.getResult().apply(this, accessSpecifier); // PTypeAssistantTC.narrowerThan(type.getResult(),accessSpecifier);
	}

	@Override
	public Boolean caseAOperationType(AOperationType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		for (PType t : type.getParameters())
		{
			if (t.apply(this, accessSpecifier))// (PTypeAssistantTC.narrowerThan(t, accessSpecifier))
			{
				return true;
			}
		}

		return type.getResult().apply(this, accessSpecifier); // PTypeAssistantTC.narrowerThan(type.getResult(),accessSpecifier);
	}

	@Override
	public Boolean caseAOptionalType(AOptionalType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		// return AOptionalTypeAssistantTC.narrowerThan(type, accessSpecifier);
		return type.getType().apply(this, accessSpecifier);
	}

	@Override
	public Boolean defaultSSeqType(SSeqType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		return type.getSeqof().apply(this, accessSpecifier);
	}

	@Override
	public Boolean caseASetType(ASetType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		return type.getSetof().apply(this, accessSpecifier);
	}

	@Override
	public Boolean caseAUnionType(AUnionType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		for (PType t : type.getTypes())
		{
			if (t.apply(this, accessSpecifier)) // (PTypeAssistantTC.narrowerThan(t, accessSpecifier))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public Boolean caseAUnknownType(AUnknownType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		return false;
	}

	@Override
	public Boolean caseANamedInvariantType(ANamedInvariantType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		if (type.getInNarrower())
		{
			return false;
		}

		type.setInNarrower(true);
		boolean result = false;

		if (type.getDefinitions().size() > 0)
		{
			for (PDefinition d : type.getDefinitions())
			{
				if (PAccessSpecifierAssistantTC.narrowerThan(d.getAccess(), accessSpecifier))
				{
					result = true;
					break;
				}
			}
		} else if (type.getType().getDefinitions().size() == 0)
		{
			result = type.apply(this, accessSpecifier)
					|| PTypeAssistantTC.narrowerThanBaseCase(type, accessSpecifier);// PTypeAssistantTC.narrowerThan(type,
																					// accessSpecifier)
		} else
		{
			for (PDefinition d : type.getType().getDefinitions())
			{
				if (PAccessSpecifierAssistantTC.narrowerThan(d.getAccess(), accessSpecifier))
				{
					result = true;
					break;
				}
			}

		}

		type.setInNarrower(false);
		return result;
	}

	@Override
	public Boolean caseARecordInvariantType(ARecordInvariantType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		if (type.getInNarrower())
		{
			return false;
		} else
		{
			type.setInNarrower(true);
		}

		boolean result = false;

		if (type.getDefinitions().size() > 0)
		{
			for (PDefinition d : type.getDefinitions())
			{
				if (PAccessSpecifierAssistantTC.narrowerThan(d.getAccess(), accessSpecifier))
				{
					result = true;
					break;
				}
			}
		} else
		{
			for (AFieldField field : type.getFields())
			{
				if (field.getType().apply(this, accessSpecifier))// (PTypeAssistantTC.narrowerThan(field.getType(),
																	// accessSpecifier))
				{
					result = true;
					break;
				}
			}
		}

		type.setInNarrower(false);
		return result;
	}

	// FIXME: IN PTypeAssistantTC the SInvariatType is SInvariantTypeBase. ASK
	@Override
	public Boolean defaultSInvariantType(SInvariantType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		return af.createPTypeAssistant().narrowerThanBaseCase(type, accessSpecifier);
	}

	@Override
	public Boolean defaultPType(PType type,
			AAccessSpecifierAccessSpecifier accessSpecifier)
			throws AnalysisException
	{
		return af.createPTypeAssistant().narrowerThanBaseCase(type, accessSpecifier);
	}

	@Override
	public Boolean createNewReturnValue(INode node,
			AAccessSpecifierAccessSpecifier question) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean createNewReturnValue(Object node,
			AAccessSpecifierAccessSpecifier question) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
