package aii.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import aii.Mesaj;

// TODO: Auto-generated Javadoc
/**
 * The Class ObjectTableModel.
 *
 * @param <T> the generic type
 * @author cosmin
 */
@SuppressWarnings("serial")
public class ObjectTableGenerator<T> extends AbstractTableModel {

	/** The objects. */
	private ArrayList<T> objects;

	/** The column names. */
	private String[] columnNames;
	
	/** The fields. */
	private Field[] fields;
	
	/** The indexes of the fields which will be used to id a given row, when using the edit/delete column. */
	private int[] idFields;
	
	/** The id names. */
	private String[] idNames;


	/**
	 * Instantiates a new object table generator.
	 *
	 * @param classType the class type
	 * @param objects the objects
	 * @param columnNames the column names
	 * @param fieldNames the field names
	 * @param idFields the fields which will be used for the generation of the id
	 * @param idNames the id names
	 * @throws SecurityException the security exception
	 * @throws NoSuchFieldException the no such field exception
	 */
	public ObjectTableGenerator(Class<T> classType, ArrayList<T> objects, String[] columnNames, String[] fieldNames, int[] idFields, String[] idNames) throws SecurityException, NoSuchFieldException {
		this.objects = objects;
		this.columnNames = columnNames;
		this.idFields=idFields;
		this.idNames=idNames;
		fields=new Field[fieldNames.length];
		
		//Build the object fields
		for(int i=0;i<fieldNames.length;i++)
		{
			try{
			fields[i]=classType.getDeclaredField(fieldNames[i]);
			}
			catch(NoSuchFieldException ex)
			{
				fields[i]=classType.getSuperclass().getDeclaredField(fieldNames[i]);
			}
		}
		
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return objects.size();
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		T object=objects.get(rowIndex);
		
		try {
			return fields[columnIndex].get(object);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}

	/**
	 * Sets the objects.
	 *
	 * @param objects the new objects
	 */
	public void setObjects(ArrayList<T> objects) {
		this.objects = objects;
	}
	
	public enum ControlType {Checkbox, Radio};
	
	/**
	 * Gets the HTML table representation. Without the table tag
	 *
	 * @param editLink the edit link
	 * @param deleteLink the delete link
	 * @return the hTML table representation
	 */
	public String getHTMLTableRepresentation(String editLink, String deleteLink)
	{
		return getHTMLTableRepresentation(editLink, deleteLink, null,null);
	}
	
	/**
	 * Gets the HTML table representation. Without the table tag. Only the first id is used for the control.
	 *
	 * @param editLink the edit link
	 * @param deleteLink the delete link
	 * @return the hTML table representation
	 */
	public String getHTMLTableRepresentation(String editLink, String deleteLink, ControlType control, String name)
	{
		StringBuilder result=new StringBuilder();
		//Build the header first
		result.append("\t<tr>");
		//Select column
		if(control!=null)
			result.append("\t\t<th>Sel</th>");
		for(int i=0;i<getColumnCount();i++)
			result.append("\t\t<th>"+getColumnName(i)+"</th>");
		//Admin column
		if(editLink!=null || deleteLink!=null)
			result.append("\t\t<th>Admin</th>");

		result.append("</tr>\n");

		//Add the data
		for(int i=0; i<getRowCount();i++)
		{
			result.append("\t<tr>");
			//Select column
			if(control!=null)
			{
				result.append("\t\t<td>");
				if(control==ControlType.Radio)
					result.append("<input type='radio' name='"+name+"' value='"+getValueAt(i, idFields[0])+"'/>");
				else if(control==ControlType.Checkbox)
					result.append("<input type='checkbox' name='"+name+"' value='"+getValueAt(i, idFields[0])+"'/>");
				result.append("</td>");
			}
			//Data columns
			for (int j = 0; j < getColumnCount(); j++)
			{
				if(getValueAt(i, j)==null)
					result.append("\t\t<td> - </td>");	
				else
				{
					if(editLink!=null && editLink.contains("read"))
					{
						Boolean citit=(Boolean) getValueAt(i, 5);		
						if(!citit)
							result.append("\t\t<td><b>" + getValueAt(i, j) + "<b></td>");
						else
							result.append("\t\t<td>" + getValueAt(i, j) + "</td>");
					}
					else
						result.append("\t\t<td>" + getValueAt(i, j) + "</td>");
				}
			}
			//Admin columns
			if(editLink!=null || deleteLink!=null)
			{
				result.append("\t\t<td>");
				String id=buildIdentificationString(i);
				String text="Edit";
				if(editLink!=null && editLink.contains("read"))
				{
					Boolean citit=(Boolean) getValueAt(i, 5);
					if(!citit)
						text="Mark as Read";
					else
						text="";
				}
				if(editLink!=null)
					result.append("<a href='"+editLink+"?"+id+"'>"+text+"</a> ");
				if(deleteLink!=null)
					result.append("<a href='"+deleteLink+"?"+id+"'>Delete</a>");
				result.append("</td>");
			}

			result.append("</tr>");
		}
		
		return result.toString();
	}

	/**
	 * Builds the identification string, considering the primary key, for a given row.
	 *
	 * @param row the row
	 * @return the string
	 */
	private String buildIdentificationString(int row)
	{
		String result="";
		for(int i=0;i<idFields.length;i++)
			result+=idNames[i]+"="+getValueAt(row, idFields[i])+"&";
		return result.substring(0,result.length()-1);
	}
}
