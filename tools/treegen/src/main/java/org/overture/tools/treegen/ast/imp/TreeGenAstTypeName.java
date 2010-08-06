// this file is automatically generated by treegen. do not modify!

package org.overture.tools.treegen.ast.imp;

// import the abstract tree interfaces
import org.overture.tools.treegen.ast.itf.*;

public class TreeGenAstTypeName extends TreeGenAstTypeSpecification implements ITreeGenAstTypeName
{
	// private member variable (name)
	private String m_name = new String();

	// public operation to retrieve the embedded private field value
	public String getName()
	{
		return m_name;
	}

	// public operation to set the embedded private field value
	public void setName(String p_name)
	{
		// consistency check (field must be non null!)
		assert(p_name != null);

		// instantiate the member variable
		m_name = p_name;
	}

	// default constructor
	public TreeGenAstTypeName()
	{
		super();
		m_name = null;
	}

	// auxiliary constructor
	public TreeGenAstTypeName(
		String p_name
	) {
		super();
		setName(p_name);
	}

	// visitor support
	public void accept(ITreeGenAstVisitor pVisitor) { pVisitor.visitTypeName(this); }

	// the identity function
	public String identify() { return "TreeGenAstTypeName"; }
}
