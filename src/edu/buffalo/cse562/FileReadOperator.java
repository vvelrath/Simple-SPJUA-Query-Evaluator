package edu.buffalo.cse562;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import net.sf.jsqlparser.schema.Column;
public class FileReadOperator implements IOperator {

	BufferedReader input;
	File f;
	HashMap<Integer,String> colType;
	HashMap<String,String> colDetails;
	Column[] schema=null;

	public FileReadOperator(File f, HashMap<Integer,String> colType, HashMap<String,String> colDetails, Column[] schema) {
		this.f = f;
		this.colType = colType;
		this.colDetails = colDetails;
		resetStream();
		this.schema=schema;
	}

	@Override
	public void resetStream() {
		try {
			input = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			input = null;
		}

	}

	@Override
	public IDatum[] readOneTuple() {
		if (input == null)
			return null;
		String tuple = null;
		String type = null;
		String aDelimiter = "[|\\n]";
		try {
			tuple = input.readLine();
			// TODO: handle EOF
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (tuple == null)
			return null;
		if(tuple.isEmpty()){
			return readOneTuple();
		}
		String[] aValueList = tuple.split(aDelimiter);
		IDatum[] colValue = new IDatum[aValueList.length];
		for(int i=0;i<aValueList.length;i++){
			//type = colType.get(i);
			type = colDetails.get(schema[i].getColumnName());
			// how about data types?
			// decimal, char, varchar? 
			//Is char and varchar automatically treated as string?
			switch(type.toLowerCase()) { 
			case "int": colValue[i] = new integerDatum(aValueList[i]); break;
			case "boolean": colValue[i] = new booleanDatum(aValueList[i]); break;
			case "date": colValue[i] = new dateDatum(aValueList[i]); break;
			case "double": colValue[i] = new doubleDatum(aValueList[i]); break;
			case "string": colValue[i] = new stringDatum(aValueList[i]); break;
			}
		}
		return colValue;

	}
	
	@Override
	public Column[] getSchema() {
		// TODO Auto-generated method stub
		return schema;
	}

	@Override
	public void setSchema(Column[] col) {
		// TODO Auto-generated method stub
		this.schema = col;
	}
}
