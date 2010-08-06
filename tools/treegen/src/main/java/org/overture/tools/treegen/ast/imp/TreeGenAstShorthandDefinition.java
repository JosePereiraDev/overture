// this file is automatically generated by treegen. do not modify!

package org.overture.tools.treegen.ast.imp;

// import the abstract tree interfaces
import org.overture.tools.treegen.ast.itf.*;

public class TreeGenAstShorthandDefinition extends TreeGenAstDefinitions implements ITreeGenAstShorthandDefinition
{
	// private member variable (shorthand_name)
	private String m_shorthand_name = new String();

	// public operation to retrieve the embedded private field value
	public String getShorthandName()
	{
		return m_shorthand_name;
	}

	// public operation to set the embedded private field value
	public void setShorthandName(String p_shorthand_name)
	{
		// consistency check (field must be non null!)
		assert(p_shorthand_name != null);

		// instantiate the member variable
		m_shorthand_name = p_shorthand_name;
	}

	// private member variable (type)
	private ITreeGenAstTypeSpecification m_type = null;

	// public operation to retrieve the embedded private field value
	public ITreeGenAstTypeSpecification getType()
	{
		return m_type;
	}

	// public operation to set the embedded private field value
	public void setType(ITreeGenAstTypeSpecification p_type)
	{
		// consistency check (field must be non null!)
		assert(p_type != null);

		// instantiate the member variable
		m_type = p_type;

		// set the parent of the parameter passed
		p_type.setParent(this);
	}

	// default constructor
	public TreeGenAstShorthandDefinition()
	{
		super();
		m_shorthand_name = null;
		m_type = null;
	}

	// auxiliary constructor
	public TreeGenAstShorthandDefinition(
		String p_shorthand_name,
		ITreeGenAstTypeSpecification p_type
	) {
		super();
		setShorthandName(p_shorthand_name);
		setType(p_type);
	}

	// visitor support
	public void accept(ITreeGenAstVisitor pVisitor) { pVisitor.visitShorthandDefinition(this); }

	// the identity function
	public String identify() { return "TreeGenAstShorthandDefinition"; }
}
