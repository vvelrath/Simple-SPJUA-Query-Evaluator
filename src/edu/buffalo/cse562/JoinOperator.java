package edu.buffalo.cse562;

import java.util.ArrayList;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class JoinOperator implements IOperator {
	IOperator left;
	IOperator right;
	IDatum[] leftValues = null;
	IDatum[] rightValues = null;
	Expression onExpression=null;
	
	public JoinOperator(IOperator left, IOperator right,Expression onExpression, Column[] right_schema) {
		this.left = left;
		this.right = right;
		this.onExpression=onExpression;
		
		left.resetStream();
		leftValues = left.readOneTuple();
			
		ArrayList<Column> schlst = new ArrayList<Column>();
		Column[] left_schema = left.getSchema();
		for(int i=0;i<left_schema.length;i++)
		{
			schlst.add(left_schema[i]);
		}
		for(int i=0;i<right_schema.length;i++)
		{
			schlst.add(right_schema[i]);
		}
		Column[] schema_loc = new Column[schlst.size()];
		schema_loc = schlst.toArray(schema_loc);
		this.setSchema(schema_loc);
	}

	@Override
	public void resetStream() {
		right.resetStream();
	}

	@Override
	public IDatum[] readOneTuple() {
		IDatum[] colValues=null;
	do{
		rightValues = right.readOneTuple();
		
		if (rightValues == null) {
			//has reached EOF
			resetStream();
			leftValues = left.readOneTuple();
			rightValues = right.readOneTuple();
		}
		
		if (leftValues == null) {
			//has reached EOF
			return null;
		}
		
		ArrayList<IDatum> commonList = new ArrayList<IDatum>();
		
		
		for(int i=0;i<leftValues.length;i++)
		{
			commonList.add(leftValues[i]);	
		}
		
		
		for(int i=0;i<rightValues.length;i++)
		{
			commonList.add(rightValues[i]);
		}
		
		
		 colValues = new IDatum[commonList.size()];
		colValues = commonList.toArray(colValues);
		
		StatementEvaluator evaluator=new StatementEvaluator(this.getSchema(),colValues);
		onExpression.accept(evaluator);
		if (!(evaluator.getResult() == true)) {
			colValues = null;
		}
		
		
	} while(colValues==null);
		
		return colValues;
	}

	@Override
	public Column[] getSchema() {
		// TODO Auto-generated method stub
		return left.getSchema();
	}

	@Override
	public void setSchema(Column[] col) {
		// TODO Auto-generated method stub
		left.setSchema(col);
	}

	

}
