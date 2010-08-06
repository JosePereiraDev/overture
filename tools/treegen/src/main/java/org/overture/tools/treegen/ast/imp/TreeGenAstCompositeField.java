// this file is automatically generated by treegen. do not modify!

package org.overture.tools.treegen.ast.imp;

// import the abstract tree interfaces
import org.overture.tools.treegen.ast.itf.*;

public class TreeGenAstCompositeField extends TreeGenAstNode implements ITreeGenAstCompositeField
{
	// private member variable (field_name)
	private String m_field_name = new String();

	// public operation to retrieve the embedded private field value
	public String getFieldName()
	{
		return m_field_name;
	}

	// public operation to set the embedded private field value
	public void setFieldName(String p_field_name)
	{
		// consistency check (field must be non null!)
		assert(p_field_name != null);

		// instantiate the member variable
		m_field_name = p_field_name;
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

	// private member variable (value)
	private String m_value = new String();

	// public operation to retrieve the embedded private field value
	public String getValue()
	{
		return m_value;
	}

	// public operation to set the embedded private field value
	public void setValue(String p_value)
	{
		// consistency check (field must be non null!)
		assert(p_value != null);

		// instantiate the member variable
		m_value = p_value;
	}

	// default constructor
	public TreeGenAstCompositeField()
	{
		super();
		m_field_name = null;
		m_type = null;
		m_value = null;
	}

	// auxiliary constructor
	public TreeGenAstCompositeField(
		String p_field_name,
		ITreeGenAstTypeSpecification p_type,
		String p_value
	) {
		super();
		setFieldName(p_field_name);
		setType(p_type);
		setValue(p_value);
	}

	// visitor support
	public void accept(ITreeGenAstVisitor pVisitor) { pVisitor.visitCompositeField(this); }

	// the identity function
	public String identify() { return "TreeGenAstCompositeField"; }
}
