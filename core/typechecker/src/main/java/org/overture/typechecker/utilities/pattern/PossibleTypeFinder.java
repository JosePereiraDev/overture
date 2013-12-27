package org.overture.typechecker.utilities.pattern;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.AnswerAdaptor;
import org.overture.ast.assistant.pattern.PTypeList;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.ABooleanPattern;
import org.overture.ast.patterns.ACharacterPattern;
import org.overture.ast.patterns.AConcatenationPattern;
import org.overture.ast.patterns.AExpressionPattern;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.AIgnorePattern;
import org.overture.ast.patterns.AIntegerPattern;
import org.overture.ast.patterns.ANilPattern;
import org.overture.ast.patterns.AQuotePattern;
import org.overture.ast.patterns.ARealPattern;
import org.overture.ast.patterns.ARecordPattern;
import org.overture.ast.patterns.ASeqPattern;
import org.overture.ast.patterns.ASetPattern;
import org.overture.ast.patterns.AStringPattern;
import org.overture.ast.patterns.ATuplePattern;
import org.overture.ast.patterns.AUnionPattern;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.types.ASeqSeqType;
import org.overture.ast.types.PType;
import org.overture.ast.util.PTypeSet;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.pattern.PPatternAssistantTC;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;
import org.overture.typechecker.assistant.type.SNumericBasicTypeAssistantTC;

/**
 * Used to get if a possible type out of a pattern.
 * 
 * @author kel
 */
public class PossibleTypeFinder extends AnswerAdaptor<PType>
{
	protected ITypeCheckerAssistantFactory af;

	public PossibleTypeFinder(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	@Override
	public PType caseABooleanPattern(ABooleanPattern pattern)
			throws AnalysisException
	{
		return AstFactory.newABooleanBasicType(pattern.getLocation());
	}

	@Override
	public PType caseACharacterPattern(ACharacterPattern pattern)
			throws AnalysisException
	{
		return AstFactory.newACharBasicType(pattern.getLocation());
	}

	@Override
	public PType caseAConcatenationPattern(AConcatenationPattern pattern)
			throws AnalysisException
	{
		return AstFactory.newASeqSeqType(pattern.getLocation(), AstFactory.newAUnknownType(pattern.getLocation()));
	}

	@Override
	public PType caseAExpressionPattern(AExpressionPattern pattern)
			throws AnalysisException
	{
		return AstFactory.newAUnknownType(pattern.getLocation());
	}

	@Override
	public PType caseAIdentifierPattern(AIdentifierPattern pattern)
			throws AnalysisException
	{
		// return AIdentifierPatternAssistantTC.getPossibleTypes(pattern);
		return AstFactory.newAUnknownType(pattern.getLocation());
	}

	@Override
	public PType caseAIgnorePattern(AIgnorePattern pattern)
			throws AnalysisException
	{
		return AstFactory.newAUnknownType(pattern.getLocation());
	}

	@Override
	public PType caseAIntegerPattern(AIntegerPattern pattern)
			throws AnalysisException
	{
		return SNumericBasicTypeAssistantTC.typeOf(pattern.getValue().getValue(), pattern.getLocation());
	}

	@Override
	public PType caseANilPattern(ANilPattern pattern) throws AnalysisException
	{
		return AstFactory.newAOptionalType(pattern.getLocation(), AstFactory.newAUnknownType(pattern.getLocation()));
	}

	@Override
	public PType caseAQuotePattern(AQuotePattern pattern)
			throws AnalysisException
	{
		return AstFactory.newAQuoteType(((AQuotePattern) pattern).getValue().clone());
	}

	@Override
	public PType caseARealPattern(ARealPattern pattern)
			throws AnalysisException
	{
		return AstFactory.newARealNumericBasicType(pattern.getLocation());
	}

	@Override
	public PType caseARecordPattern(ARecordPattern pattern)
			throws AnalysisException
	{
		return pattern.getType();
	}

	@Override
	public PType caseASetPattern(ASetPattern pattern) throws AnalysisException
	{
		return AstFactory.newASetType(pattern.getLocation(), AstFactory.newAUnknownType(pattern.getLocation()));
	}

	@Override
	public PType caseASeqPattern(ASeqPattern pattern) throws AnalysisException
	{
		return AstFactory.newASeqSeqType(pattern.getLocation(), AstFactory.newAUnknownType(pattern.getLocation()));
	}

	@Override
	public PType caseAStringPattern(AStringPattern pattern)
			throws AnalysisException
	{
		ASeqSeqType t = AstFactory.newASeqSeqType(pattern.getLocation(), AstFactory.newACharBasicType(pattern.getLocation()));
		return t;
	}

	@Override
	public PType caseATuplePattern(ATuplePattern pattern)
			throws AnalysisException
	{
		PTypeList list = new PTypeList();

		for (PPattern p : pattern.getPlist())
		{
			list.add(PPatternAssistantTC.getPossibleType(p));
		}

		return list.getType(pattern.getLocation());
	}

	@Override
	public PType caseAUnionPattern(AUnionPattern pattern)
			throws AnalysisException
	{
		PTypeSet set = new PTypeSet();

		set.add(PPatternAssistantTC.getPossibleType(pattern.getLeft()));
		set.add(PPatternAssistantTC.getPossibleType(pattern.getRight()));

		PType s = set.getType(pattern.getLocation());

		return PTypeAssistantTC.isUnknown(s) ? AstFactory.newASetType(pattern.getLocation(), AstFactory.newAUnknownType(pattern.getLocation()))
				: s;
	}

	@Override
	public PType defaultPPattern(PPattern pattern) throws AnalysisException
	{
		return null;
	}

	@Override
	public PType createNewReturnValue(INode node) throws AnalysisException
	{
		assert false : "Should not happen";
		return null;
	}

	@Override
	public PType createNewReturnValue(Object node) throws AnalysisException
	{
		assert false : "Should not happen";
		return null;
	}

}
