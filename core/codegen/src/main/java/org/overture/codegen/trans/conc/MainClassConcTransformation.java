/**
 * 
 */
package org.overture.codegen.trans.conc;

import java.util.List;

import org.overture.codegen.cgast.SStmCG;
import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.analysis.DepthFirstAnalysisAdaptor;
import org.overture.codegen.cgast.declarations.AClassDeclCG;
import org.overture.codegen.cgast.declarations.AFieldDeclCG;
import org.overture.codegen.cgast.declarations.AMethodDeclCG;
import org.overture.codegen.cgast.statements.ACallStmCG;
import org.overture.codegen.cgast.types.AClassTypeCG;
import org.overture.codegen.ir.IRInfo;

/**
 * @author gkanos
 *
 */
public class MainClassConcTransformation extends DepthFirstAnalysisAdaptor
{
	private IRInfo info;
	private List<AClassDeclCG> classes;

	public MainClassConcTransformation(IRInfo info, List<AClassDeclCG> classes)
	{
		this.info = info;
		this.classes = classes;
	}
	
	@Override
	public void caseAClassDeclCG(AClassDeclCG node) throws AnalysisException
	{
		if(!info.getSettings().generateConc())
		{
			return;
		}
		
//		for(AFieldDeclCG x : node.getFields())
//		{
//			//x.s
//		}
		
		for(AMethodDeclCG x : node.getMethods())
		{
			if (x.getName() != "toString"){
				ACallStmCG entering = new ACallStmCG();
				ACallStmCG leaving = new ACallStmCG();
			
				entering.setName("entering");
				AClassTypeCG sentinel = new AClassTypeCG();
				sentinel.setName("Sentinel");
				entering.setClassType(sentinel);
			
				leaving.setName("leaving");
				leaving.setClassType(sentinel.clone());
			
			
				//	x.setBody(entering);
				x.setBody(x.getBody());
				//x.setBody(leaving);
			}
			else
			{
				continue;
			}
		}
	}
}
