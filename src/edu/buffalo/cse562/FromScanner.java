package edu.buffalo.cse562;


import java.io.File;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.schema.*;

public class FromScanner implements FromItemVisitor{

	File basePath;
	HashMap<String,CreateTable> tables;
	public static HashMap<Integer,String> colType = new HashMap<Integer,String>();
	public static HashMap<String,String> colDetails = new HashMap<String,String>();
	public static HashMap<String,Integer> colIndex = new HashMap<String,Integer>();
	public Column[] schema=null;
	public IOperator source=null;
	
	public FromScanner(File basePath,HashMap<String,CreateTable> tables)
	{
	this.basePath=basePath;
	this.tables=tables;
	}
	
	@Override
	public void visit(Table tableName) {
		//instantiate a 'createTable' instance with the table 'tableName' from the hash map
	CreateTable table=tables.get(tableName.getName().toUpperCase());
	List<?> cols=table.getColumnDefinitions();
	
		
	schema=new Column[cols.size()];
	for(int i=0;i<cols.size();i++)
	{
		ColumnDefinition colDef=(ColumnDefinition)cols.get(i);
		
		String type = colDef.getColDataType().toString();
		
		if(type.toLowerCase().startsWith("decimal"))
		{
			ColDataType dataType = new ColDataType();
			dataType.setDataType("DOUBLE");
			colDef.setColDataType(dataType);
		}
				
		if(type.toLowerCase().startsWith("varchar")||type.toLowerCase().startsWith("char"))
		{
			ColDataType dataType = new ColDataType();
			dataType.setDataType("STRING");
			colDef.setColDataType(dataType);
		}
		
		colType.put(i,colDef.getColDataType().toString());
		colDetails.put(colDef.getColumnName(), colDef.getColDataType().toString());
		colIndex.put(colDef.getColumnName(), i);
		// populate the schema array to contain column names 
		schema[i]=new Column(tableName,colDef.getColumnName());
	}
	//create a class instead of the one below
	source=new FileReadOperator(new File(basePath,tableName.getName()+".dat"), colType, colDetails, schema);
		
	}

	@Override
	public void visit(SubSelect subselect) {
		// TODO Auto-generated method stub
		SelectBody select = (SelectBody)subselect.getSelectBody();
		
		if (select instanceof PlainSelect) {

			PlainSelect pselect = (PlainSelect) select;
			SelectEvaluator sel_visitor = new SelectEvaluator(source, basePath, tables);
			pselect.accept(sel_visitor);
			source=sel_visitor.operator;
		}
	}

	@Override
	public void visit(SubJoin arg0) {
		// TODO Auto-generated method stub
		
	}

}
