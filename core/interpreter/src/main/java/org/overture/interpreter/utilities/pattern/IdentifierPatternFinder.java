package org.overture.interpreter.utilities.pattern;

import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.AnswerAdaptor;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.AConcatenationPattern;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.AMapPattern;
import org.overture.ast.patterns.AMapUnionPattern;
import org.overture.ast.patterns.AMapletPatternMaplet;
import org.overture.ast.patterns.ARecordPattern;
import org.overture.ast.patterns.ASeqPattern;
import org.overture.ast.patterns.ASetPattern;
import org.overture.ast.patterns.ATuplePattern;
import org.overture.ast.patterns.AUnionPattern;
import org.overture.ast.patterns.PPattern;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.assistant.pattern.AMapPatternMapletAssistantInterpreter;

/***************************************
 * 
 * This class implement a way to find identifier for patterns in a pattern type
 * 
 * @author gkanos
 *
 ****************************************/
public class IdentifierPatternFinder extends AnswerAdaptor<List<AIdentifierPattern>>
{
	protected IInterpreterAssistantFactory af;
	
	public IdentifierPatternFinder(IInterpreterAssistantFactory af)
	{
		this.af = af;
	}
	
	@Override
	public List<AIdentifierPattern> caseAConcatenationPattern(
			AConcatenationPattern pattern) throws AnalysisException
	{
		//return AConcatenationPatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();
		//list.addAll(PPatternAssistantInterpreter.findIdentifiers(pattern.getLeft()));
		//list.addAll(PPatternAssistantInterpreter.findIdentifiers(pattern.getRight()));
		list.addAll(pattern.getLeft().apply(THIS));
		list.addAll(pattern.getRight().apply(THIS));
		return list;
	}

	@Override
	public List<AIdentifierPattern> caseAIdentifierPattern(
			AIdentifierPattern pattern) throws AnalysisException
	{
		//return AIdentifierPatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();
		list.add(pattern);
		return list;
	}
	@Override
	public List<AIdentifierPattern> caseAMapPattern(AMapPattern pattern)
			throws AnalysisException
	{
		//return AMapPatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();

		for (AMapletPatternMaplet p : pattern.getMaplets())
		{
			list.addAll(AMapPatternMapletAssistantInterpreter.findIdentifiers(p));
		}

		return list;
	}
	
	@Override
	public List<AIdentifierPattern> caseAMapUnionPattern(AMapUnionPattern pattern)
			throws AnalysisException
	{
		//return AMapUnionPatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();
		//list.addAll(PPatternAssistantInterpreter.findIdentifiers(pattern.getLeft()));
		//list.addAll(PPatternAssistantInterpreter.findIdentifiers(pattern.getRight()));
		list.addAll(pattern.getLeft().apply(THIS));
		list.addAll(pattern.getRight().apply(THIS));
		return list;
	}
	
	@Override
	public List<AIdentifierPattern> caseARecordPattern(ARecordPattern pattern)
			throws AnalysisException
	{
		//return ARecordPatternAssistantInterpreter.findIndentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();

		for (PPattern p : pattern.getPlist())
		{
			//list.addAll(PPatternAssistantInterpreter.findIdentifiers(p));
			list.addAll(p.apply(THIS));
		}

		return list;
	}
	
	@Override
	public List<AIdentifierPattern> caseASeqPattern(ASeqPattern pattern)
			throws AnalysisException
	{
		//return ASeqPatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();

		for (PPattern p : pattern.getPlist())
		{
			//list.addAll(PPatternAssistantInterpreter.findIdentifiers(p));
			list.addAll(p.apply(THIS));
		}

		return list;
	}
	
	@Override
	public List<AIdentifierPattern> caseASetPattern(ASetPattern pattern)
			throws AnalysisException
	{
		//return ASetPatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();

		for (PPattern p : pattern.getPlist())
		{
			//list.addAll(PPatternAssistantInterpreter.findIdentifiers(p));
			list.addAll(p.apply(THIS));
		}

		return list;
	}
	
	@Override
	public List<AIdentifierPattern> caseATuplePattern(ATuplePattern pattern)
			throws AnalysisException
	{
		//return ATuplePatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();

		for (PPattern p : pattern.getPlist())
		{
			//list.addAll(PPatternAssistantInterpreter.findIdentifiers(p));
			list.addAll(p.apply(THIS));
		}

		return list;
	}
	
	@Override
	public List<AIdentifierPattern> caseAUnionPattern(AUnionPattern pattern)
			throws AnalysisException
	{
		//return AUnionPatternAssistantInterpreter.findIdentifiers(pattern);
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();
		//list.addAll(PPatternAssistantInterpreter.findIdentifiers(pattern.getLeft()));
		//list.addAll(PPatternAssistantInterpreter.findIdentifiers(pattern.getRight()));
		list.addAll(pattern.getLeft().apply(THIS));
		list.addAll(pattern.getRight().apply(THIS));
		return list;
	}
	
	@Override
	public List<AIdentifierPattern> defaultPPattern(PPattern node)
			throws AnalysisException
	{
		return new Vector<AIdentifierPattern>(); // Most have none
	}

	@Override
	public List<AIdentifierPattern> createNewReturnValue(INode node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AIdentifierPattern> createNewReturnValue(Object node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
