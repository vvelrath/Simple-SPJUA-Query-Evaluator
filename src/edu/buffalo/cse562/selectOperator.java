package edu.buffalo.cse562;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class selectOperator implements IOperator {
	IOperator input;
	Expression condition;
	Column[] schema;

	public selectOperator(IOperator input,Column[] schema,Expression condition) {
		this.input = input;
		this.condition = condition;
		this.schema=schema;
	}

	@Override
	public void resetStream() {
		input.resetStream();
	}

	@Override
	public IDatum[] readOneTuple() {
		
		IDatum[] colValues = null;
				
		do {
			colValues = input.readOneTuple();
			if (colValues == null) {
				//has reached EOF
				return null;
			} 
			
			StatementEvaluator evaluator=new StatementEvaluator(schema,colValues);
			condition.accept(evaluator);
			if (!(evaluator.getResult() == true)) {
				colValues = null;
			}
			
		} while (colValues == null);
		return colValues;
	}
	
	@Override
	public Column[] getSchema() {
		// TODO Auto-generated method stub
		return input.getSchema();
	}

	@Override
	public void setSchema(Column[] col) {
		// TODO Auto-generated method stub
		this.setSchema(col);
	}
	
}
