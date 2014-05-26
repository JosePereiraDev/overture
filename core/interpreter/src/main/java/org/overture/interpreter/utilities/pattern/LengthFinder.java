package org.overture.interpreter.utilities.pattern;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.AnswerAdaptor;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.AConcatenationPattern;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.AIgnorePattern;
import org.overture.ast.patterns.AMapPattern;
import org.overture.ast.patterns.AMapUnionPattern;
import org.overture.ast.patterns.ASeqPattern;
import org.overture.ast.patterns.ASetPattern;
import org.overture.ast.patterns.AStringPattern;
import org.overture.ast.patterns.AUnionPattern;
import org.overture.ast.patterns.PPattern;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.assistant.pattern.PPatternAssistantInterpreter;

/***************************************
 * 
 * This class implement a way to find the length of a pattern?!?!? still not sure what this method do.
 * 
 * @author gkanos
 *
 ****************************************/
public class LengthFinder extends AnswerAdaptor<Integer>
{
	protected IInterpreterAssistantFactory af;
	
	public LengthFinder(IInterpreterAssistantFactory af)
	{
		this.af = af;
	}
	
	@Override
	public Integer caseAConcatenationPattern(AConcatenationPattern pattern)
			throws AnalysisException
	{
		//return AConcatenationPatternAssistantInterpreter.getLength(pattern);
		int llen = pattern.getLeft().apply(THIS);//PPatternAssistantInterpreter.getLength(pattern.getLeft());
		int rlen = pattern.getRight().apply(THIS);//PPatternAssistantInterpreter.getLength(pattern.getRight());
		return llen == PPatternAssistantInterpreter.ANY
				|| rlen == PPatternAssistantInterpreter.ANY ? PPatternAssistantInterpreter.ANY
				: llen + rlen;
	}
	
	@Override
	public Integer caseAIdentifierPattern(AIdentifierPattern pattern)
			throws AnalysisException
	{
		//return AIdentifierPatternAssistantInterpreter.getLength(pattern);
		return PPatternAssistantInterpreter.ANY; // Special value meaning "any length"
	}
	
	@Override
	public Integer caseAIgnorePattern(AIgnorePattern pattern)
			throws AnalysisException
	{
		//return AIgnorePatternAssistantInterpreter.getLength(pattern);
		return PPatternAssistantInterpreter.ANY; // Special value meaning "any length"
	}
	
	@Override
	public Integer caseAMapPattern(AMapPattern pattern) throws AnalysisException
	{
		//return AMapPatternAssistantInterpreter.getLength(pattern);
		return pattern.getMaplets().size();
	}
	
	@Override
	public Integer caseAMapUnionPattern(AMapUnionPattern pattern)
			throws AnalysisException
	{
		//return AMapUnionPatternAssistantInterpreter.getLength(pattern);
		int llen = pattern.getLeft().apply(THIS);//PPatternAssistantInterpreter.getLength(pattern.getLeft());
		int rlen = pattern.getRight().apply(THIS);//PPatternAssistantInterpreter.getLength(pattern.getRight());
		return llen == PPatternAssistantInterpreter.ANY
				|| rlen == PPatternAssistantInterpreter.ANY ? PPatternAssistantInterpreter.ANY
				: llen + rlen;
	}
	
	@Override
	public Integer caseASeqPattern(ASeqPattern pattern) throws AnalysisException
	{
		//return ASeqPatternAssistantInterpreter.getLength(pattern);
		return pattern.getPlist().size();
	}
	
	@Override
	public Integer caseASetPattern(ASetPattern pattern) throws AnalysisException
	{
		//return ASetPatternAssistantInterpreter.getLength(pattern);
		return pattern.getPlist().size();
	}
	
	@Override
	public Integer caseAStringPattern(AStringPattern pattern)
			throws AnalysisException
	{
		//return AStringPatternAssistantInterpreter.getLength(pattern);
		return pattern.getValue().getValue().length();
	}
	
	@Override
	public Integer caseAUnionPattern(AUnionPattern pattern)
			throws AnalysisException
	{
		//return AUnionPatternAssistantInterpreter.getLength(pattern);
		int llen = pattern.getLeft().apply(THIS);//PPatternAssistantInterpreter.getLength(pattern.getLeft());
		int rlen = pattern.getRight().apply(THIS);//PPatternAssistantInterpreter.getLength(pattern.getRight());
		return llen == PPatternAssistantInterpreter.ANY
				|| rlen == PPatternAssistantInterpreter.ANY ? PPatternAssistantInterpreter.ANY
				: llen + rlen;
	}
	
	@Override
	public Integer defaultPPattern(PPattern node) throws AnalysisException
	{
		return 1; // Most only identify one member
	}

	@Override
	public Integer createNewReturnValue(INode node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer createNewReturnValue(Object node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
