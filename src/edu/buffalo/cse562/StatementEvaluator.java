package edu.buffalo.cse562;

import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.*;

public class StatementEvaluator extends AbstractExpressionAndStatementVisitor {

	private boolean resultFlag;
	public Column[] schema;
	public IDatum[] colValues;
	private IDatum colValue;
	public String tableName;
	private String type;
	public Function aggregateFn;
	private static boolean AggFuncFlag=false;
	public String exprName=null;
	public Column AggOnColumn=null;
	public String RightdateValue = null;
	
	public HashMap<String,IDatum> myColumnDetails=new HashMap<String,IDatum>();
	public HashMap<String,IDatum> wasMyColumnDetails=new HashMap<String,IDatum>();
	public HashMap<String,String> aliasToTable=new HashMap<String,String>();
	public HashMap<String,String> columnToTable=new HashMap<String,String>();
	
	public StatementEvaluator(Column[] schema, IDatum[] colValues) {
		this.schema = schema;
		this.colValues = colValues;
		this.aggregateFn = null;
		for (int i = 0; i < colValues.length; i++) {
			//myColumnDetails.put(schema[i].getColumnName(), colValues[i]);
			myColumnDetails.put(schema[i].getTable().getName()+"|"+schema[i].getColumnName(), colValues[i]);
			wasMyColumnDetails.put(schema[i].getColumnName(), colValues[i]);
			aliasToTable.put(schema[i].getTable().getAlias(), schema[i].getTable().getName());
			columnToTable.put(schema[i].getColumnName(), schema[i].getTable().getName());
		}
	}
	public StatementEvaluator()
	{
		this.schema = null;
		this.colValues = null;
		this.aggregateFn = null;
		myColumnDetails=null;
	}
	public boolean getResult() {
		return resultFlag;
	}

	public static boolean getAggFuncFlag() {
		return AggFuncFlag;
	}
	public IDatum getColValue() {
		return colValue;
	}

	public String getType()
	{
		return type;
	}
	@Override
	public void visit(LongValue lv) {
		colValue=new integerDatum((String.valueOf(lv.getValue())));
	}
	
		
	@Override
	public void visit(Column col) {

		exprName = col.getColumnName();
				
		String table_or_alias = null;
		String column_key = null;
		String table_name = null;
		table_or_alias = col.getTable().getName();
		
		if(table_or_alias == null)
		{
			column_key=columnToTable.get(col.getColumnName())+"|"+col.getColumnName();
			table_name=columnToTable.get(col.getColumnName());
		}
		else if(aliasToTable.get(table_or_alias)!=null)
		{
			column_key=aliasToTable.get(table_or_alias)+"|"+col.getColumnName();
			table_name=aliasToTable.get(table_or_alias);
		}
		else
		{
			column_key=table_or_alias+"|"+col.getColumnName();
			table_name=table_or_alias;
		}
		if(myColumnDetails!=null){
		IDatum exprValue = myColumnDetails.get(column_key);
		colValue = exprValue;
		}
	
		tableName = table_name;
		type = FromScanner.colDetails.get(exprName);
		
	}

	@Override
	public void visit(Function func) {
		Function fn = (Function) func;
		aggregateFn = fn;
		RightdateValue = null;
		AggFuncFlag=true;
		
		if(myColumnDetails!=null){
		ExpressionList paramList = fn.getParameters();
		if(paramList!=null){
		List<?> Expressions=paramList.getExpressions();
		
		if(Expressions.get(0) instanceof Column)
		{
			AggOnColumn=(Column)paramList.getExpressions().get(0);
		}
	
		RightdateValue = Expressions.get(0).toString().replaceAll("'", "");
		
	for(int i=0;i<Expressions.size();i++){
		Expression expr=(Expression) Expressions.get(i);
		expr.accept(this);
	}
	}
		else if(paramList==null && fn.isAllColumns()){
			//handles count(*)
			AggOnColumn=(Column)schema[0];
			type="int";
		}
	}
		
		exprName=fn.getName();
		//set alias
	}

	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DoubleValue dv) {
		colValue=new doubleDatum((String.valueOf(dv.getValue())));
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Parenthesis expr) {
		expr.getExpression().accept(this);

	}

	@Override
	public void visit(StringValue sv) {
		// TODO Auto-generated method stub
		colValue=new stringDatum((String.valueOf(sv.getValue())));
	}

	@Override
	public void visit(Addition expr) {

		expr.getLeftExpression().accept(this);
		IDatum leftValue = colValue;
		expr.getRightExpression().accept(this);
		IDatum rightValue = colValue;
		switch (type.toLowerCase()) {

		case "int":
			int n1 = ((int) leftValue.getValue() + (int) rightValue.getValue());
			colValue = new integerDatum(String.valueOf(n1));
			break;

		// how to handle additions between other data types?
		// can float be considered double?
		case "double":
		case "float":
			/*double n2 = ((double) leftValue.getValue() + (double) rightValue
					.getValue());*/
			IDatum left = new doubleDatum(leftValue.getValue().toString());
            IDatum right = new doubleDatum(rightValue.getValue().toString());
            double n2 = ((double) left.getValue() + (double)right.getValue());
			colValue = new doubleDatum(String.valueOf(n2));
			break;
		}

	}

	@Override
	public void visit(Multiplication expr) {
		expr.getLeftExpression().accept(this);
		IDatum leftValue = colValue;
		expr.getRightExpression().accept(this);
		IDatum rightValue = colValue;
		switch (type.toLowerCase()) {

		case "int":
			int n1 = ((int) leftValue.getValue() * (int) rightValue.getValue());
			colValue = new integerDatum(String.valueOf(n1));
			break;
		// check if this is right
		case "double":
		case "float":
			double n2 = ((double) leftValue.getValue() * (double) rightValue
					.getValue());
			colValue = new doubleDatum(String.valueOf(n2));
			break;
		}
	}

	@Override
	public void visit(Subtraction expr) {
		expr.getLeftExpression().accept(this);
		IDatum leftValue = colValue;
		expr.getRightExpression().accept(this);
		IDatum rightValue = colValue;
		if(myColumnDetails!=null)
		{	
			switch (type.toLowerCase()) {
			// TODO: Neeti :check if this is right
			case "int":
				int n1 = ((int) leftValue.getValue() - (int) rightValue.getValue());
				colValue = new integerDatum(String.valueOf(n1));
				break;
	
			case "double":
			case "float":
				/*double n2 = ((double) leftValue.getValue() - (double) rightValue
						.getValue());*/
				IDatum left = new doubleDatum(leftValue.getValue().toString());
                IDatum right = new doubleDatum(rightValue.getValue().toString());
                double n2 = ((double) left.getValue() - (double)right.getValue());
                if((expr.getLeftExpression() instanceof DoubleValue)&&(expr.getRightExpression() instanceof DoubleValue))
				{
					n2 = (double)Math.round(n2*100)/100;
				}
                colValue = new doubleDatum(String.valueOf(n2));
				break;
			}
		}
	}

	@Override
	public void visit(AndExpression expr) {

		BinaryExpression bex = (BinaryExpression) expr;
		bex.getLeftExpression().accept(this);
		boolean leftValue = resultFlag;
		bex.getRightExpression().accept(this);
		boolean rightValue = resultFlag;

		if (leftValue && rightValue) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}

	}

	@Override
	public void visit(OrExpression expr) {
		BinaryExpression bex = (BinaryExpression) expr;
		bex.getLeftExpression().accept(this);
		boolean leftValue = resultFlag;
		bex.getRightExpression().accept(this);
		boolean rightValue = resultFlag;

		if (leftValue || rightValue) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}

	}

	@Override
	public void visit(GreaterThan expr) {

		
		IDatum LeftExprValue = null;
		IDatum RightexprDatum = null;
		String RightExprvalue = null;
		
		if(expr.getLeftExpression() instanceof BinaryExpression||(expr.getLeftExpression() instanceof Parenthesis))
		{
			expr.getLeftExpression().accept(this);
			LeftExprValue = colValue;
		}
		else if(expr.getLeftExpression() instanceof Column)
		{	
			Column Leftexpr=(Column) expr.getLeftExpression();
			type = FromScanner.colDetails.get(Leftexpr.getColumnName());
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Leftexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Leftexpr.getColumnName())+"|"+Leftexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Leftexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Leftexpr.getColumnName();
			}
			
			LeftExprValue = myColumnDetails.get(column_key);
		}
		
		if((expr.getRightExpression() instanceof BinaryExpression)||(expr.getRightExpression() instanceof Parenthesis))
		{
			expr.getRightExpression().accept(this);
			RightExprvalue = colValue.getValue().toString();
		}
		else if(expr.getRightExpression() instanceof Column)
		{	
			Column Rightexpr=(Column) expr.getRightExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Rightexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Rightexpr.getColumnName())+"|"+Rightexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Rightexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Rightexpr.getColumnName();
			}
			RightExprvalue = myColumnDetails.get(column_key).getValue().toString();
		}
		else if(expr.getRightExpression() instanceof Function)
        {    
            expr.getRightExpression().accept(this);
            RightExprvalue=RightdateValue;
        }
		else
		{	
			RightExprvalue=expr.getRightExpression().toString();
		}
		
		
		// TODO: compares all column values as strings. Might not work for other data types
				
		switch (type.toLowerCase()) {
		case "boolean":
			RightexprDatum = new booleanDatum(RightExprvalue);
			try {
				throw new ParseException();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "int":
			RightexprDatum = new integerDatum(RightExprvalue);
			break;
		case "date":
			RightexprDatum = new dateDatum(RightExprvalue);
			break;
		case "double":
			RightexprDatum = new doubleDatum(RightExprvalue);
			break;
		case "string":
			RightexprDatum = new stringDatum(RightExprvalue);
			break;
		}
		
		if (LeftExprValue.compareTo(RightexprDatum) > 0) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}
		
	}

	@Override
	public void visit(GreaterThanEquals expr) {
			
		IDatum LeftExprValue = null;
		IDatum RightexprDatum = null;
		String RightExprvalue = null;
		
		if(expr.getLeftExpression() instanceof BinaryExpression)
		{
			expr.getLeftExpression().accept(this);
			LeftExprValue = colValue;
		}
		else if(expr.getLeftExpression() instanceof Column)
		{	
			Column Leftexpr=(Column) expr.getLeftExpression();
			type = FromScanner.colDetails.get(Leftexpr.getColumnName());
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Leftexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Leftexpr.getColumnName())+"|"+Leftexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Leftexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Leftexpr.getColumnName();
			}
			
			LeftExprValue = myColumnDetails.get(column_key);
		}
		
		if(expr.getRightExpression() instanceof BinaryExpression)
		{
			expr.getRightExpression().accept(this);
			RightExprvalue = (String) colValue.getValue();
		}
		else if(expr.getRightExpression() instanceof Column)
		{	
			Column Rightexpr=(Column) expr.getRightExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Rightexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Rightexpr.getColumnName())+"|"+Rightexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Rightexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Rightexpr.getColumnName();
			}
			RightExprvalue = myColumnDetails.get(column_key).getValue().toString();
		}
		else if(expr.getRightExpression() instanceof Function)
        {    
            expr.getRightExpression().accept(this);
            RightExprvalue=RightdateValue;
        }
		else
		{	
			RightExprvalue=expr.getRightExpression().toString();
		}
		
	
				
		switch (type.toLowerCase()) {
		case "boolean":
			RightexprDatum = new booleanDatum(RightExprvalue);
			try {
				throw new ParseException();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "int":
			RightexprDatum = new integerDatum(RightExprvalue);
			break;
		case "date":
			RightexprDatum = new dateDatum(RightExprvalue);
			break;
		case "double":
			RightexprDatum = new doubleDatum(RightExprvalue);
			break;
		case "string":
			RightexprDatum = new stringDatum(RightExprvalue);
			break;
		}
		
		if (LeftExprValue.compareTo(RightexprDatum) >= 0) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}
	
	}

	@Override
	public void visit(MinorThan expr) {

		IDatum LeftExprValue = null;
		IDatum RightexprDatum = null;
		String RightExprvalue = null;
		
		if(expr.getLeftExpression() instanceof BinaryExpression)
		{
			expr.getLeftExpression().accept(this);
			LeftExprValue = colValue;
		}
		else if(expr.getLeftExpression() instanceof Column)
		{	
			Column Leftexpr=(Column) expr.getLeftExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Leftexpr.getTable().getName();
			type = FromScanner.colDetails.get(Leftexpr.getColumnName());
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Leftexpr.getColumnName())+"|"+Leftexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Leftexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Leftexpr.getColumnName();
			}
			
			LeftExprValue = myColumnDetails.get(column_key);
		}
		
		if(expr.getRightExpression() instanceof BinaryExpression)
		{
			expr.getRightExpression().accept(this);
			RightExprvalue = colValue.getValue().toString();
		}
		else if(expr.getRightExpression() instanceof Column)
		{	
			Column Rightexpr=(Column) expr.getRightExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Rightexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Rightexpr.getColumnName())+"|"+Rightexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Rightexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Rightexpr.getColumnName();
			}
			RightExprvalue = myColumnDetails.get(column_key).getValue().toString();
		}
		else if(expr.getRightExpression() instanceof Function)
        {    
            expr.getRightExpression().accept(this);
            RightExprvalue=RightdateValue;
        }
		else
		{	
			RightExprvalue=expr.getRightExpression().toString();
		}
		
		
		// TODO: compares all column values as strings. Might not work for other data types
				
		switch (type.toLowerCase()) {
		case "boolean":
			RightexprDatum = new booleanDatum(RightExprvalue);
			try {
				throw new ParseException();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "int":
			RightexprDatum = new integerDatum(RightExprvalue);
			break;
		case "date":
			RightexprDatum = new dateDatum(RightExprvalue);
			break;
		case "double":
			RightexprDatum = new doubleDatum(RightExprvalue);
			break;
		case "string":
			RightexprDatum = new stringDatum(RightExprvalue);
			break;
		}
		
		if (LeftExprValue.compareTo(RightexprDatum) < 0) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}
	}

	@Override
	public void visit(MinorThanEquals expr) {
		
		IDatum LeftExprValue = null;
		IDatum RightexprDatum = null;
		String RightExprvalue = null;
		
		if(expr.getLeftExpression() instanceof BinaryExpression)
		{
			expr.getLeftExpression().accept(this);
			LeftExprValue = colValue;
		}
		else if(expr.getLeftExpression() instanceof Column)
		{	
			Column Leftexpr=(Column) expr.getLeftExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Leftexpr.getTable().getName();
			type = FromScanner.colDetails.get(Leftexpr.getColumnName());
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Leftexpr.getColumnName())+"|"+Leftexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Leftexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Leftexpr.getColumnName();
			}
			
			LeftExprValue = myColumnDetails.get(column_key);
		}
		
		if(expr.getRightExpression() instanceof BinaryExpression)
		{
			expr.getRightExpression().accept(this);
			RightExprvalue = (String) colValue.getValue();
		}
		else if(expr.getRightExpression() instanceof Column)
		{	
			Column Rightexpr=(Column) expr.getRightExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Rightexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Rightexpr.getColumnName())+"|"+Rightexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Rightexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Rightexpr.getColumnName();
			}
			RightExprvalue = myColumnDetails.get(column_key).getValue().toString();
		}
		else if(expr.getRightExpression() instanceof Function)
        {    
            expr.getRightExpression().accept(this);
            RightExprvalue=RightdateValue;
        }
		else
		{	
			RightExprvalue=expr.getRightExpression().toString();
		}
		
		
						
		switch (type.toLowerCase()) {
		case "boolean":
			RightexprDatum = new booleanDatum(RightExprvalue);
			try {
				throw new ParseException();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "int":
			RightexprDatum = new integerDatum(RightExprvalue);
			break;
		case "date":
			RightexprDatum = new dateDatum(RightExprvalue);
			break;
		case "double":
			RightexprDatum = new doubleDatum(RightExprvalue);
			break;
		case "string":
			RightexprDatum = new stringDatum(RightExprvalue);
			break;
		}
		
		if (LeftExprValue.compareTo(RightexprDatum) <= 0) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}
		
	}

	@Override
	public void visit(EqualsTo expr) {
		
		IDatum LeftExprValue = null;
		IDatum RightexprDatum = null;
		String RightExprvalue = null;
		
		if(expr.getLeftExpression() instanceof BinaryExpression)
		{
			expr.getLeftExpression().accept(this);
			LeftExprValue = colValue;
		}
		else if(expr.getLeftExpression() instanceof Column)
		{
			Column Leftexpr=(Column) expr.getLeftExpression();
			type = FromScanner.colDetails.get(Leftexpr.getColumnName());
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Leftexpr.getTable().getName();
			type = FromScanner.colDetails.get(Leftexpr.getColumnName());
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Leftexpr.getColumnName())+"|"+Leftexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Leftexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Leftexpr.getColumnName();
			}
			
			LeftExprValue = myColumnDetails.get(column_key);
		}
		
		if(expr.getRightExpression() instanceof BinaryExpression)
		{
			expr.getRightExpression().accept(this);
			RightExprvalue = (String) colValue.getValue();
		}
		else if(expr.getRightExpression() instanceof Column)
		{	
			Column Rightexpr=(Column) expr.getRightExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Rightexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Rightexpr.getColumnName())+"|"+Rightexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Rightexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Rightexpr.getColumnName();
			}
			
			RightExprvalue = myColumnDetails.get(column_key).getValue().toString();
		}
		else
		{	
			RightExprvalue=expr.getRightExpression().toString();
		}
		
		
				
		switch (type.toLowerCase()) {
		case "boolean":
			RightexprDatum = new booleanDatum(RightExprvalue);
			try {
				throw new ParseException();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "int":
			RightexprDatum = new integerDatum(RightExprvalue);
			break;
		case "date":
			RightexprDatum = new dateDatum(RightExprvalue);
			break;
		case "double":
			RightexprDatum = new doubleDatum(RightExprvalue);
			break;
		case "string":
			StringValue sv = (StringValue) expr.getRightExpression();
            sv.accept(this);
            RightexprDatum = colValue;
			/*RightexprDatum = new stringDatum(RightExprvalue);*/
			break;
		}
		
		if (LeftExprValue.compareTo(RightexprDatum) == 0) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}

	}

	@Override
	public void visit(NotEqualsTo expr) {
		IDatum LeftExprValue = null;
		IDatum RightexprDatum = null;
		String RightExprvalue = null;
		
		if(expr.getLeftExpression() instanceof BinaryExpression)
		{
			expr.getLeftExpression().accept(this);
			LeftExprValue = colValue;
		}
		else if(expr.getLeftExpression() instanceof Column)
		{	
			Column Leftexpr=(Column) expr.getLeftExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Leftexpr.getTable().getName();
			type = FromScanner.colDetails.get(Leftexpr.getColumnName());
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Leftexpr.getColumnName())+"|"+Leftexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Leftexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Leftexpr.getColumnName();
			}
			
			LeftExprValue = myColumnDetails.get(column_key);
		}
		
		if(expr.getRightExpression() instanceof BinaryExpression)
		{
			expr.getRightExpression().accept(this);
			RightExprvalue = (String) colValue.getValue();
		}
		else if(expr.getRightExpression() instanceof Column)
		{	
			Column Rightexpr=(Column) expr.getRightExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Rightexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Rightexpr.getColumnName())+"|"+Rightexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Rightexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Rightexpr.getColumnName();
			}
			RightExprvalue = myColumnDetails.get(column_key).getValue().toString();
		}
		else
		{	
			RightExprvalue=expr.getRightExpression().toString();
		}
		
		
				
		switch (type.toLowerCase()) {
		case "boolean":
			RightexprDatum = new booleanDatum(RightExprvalue);
			try {
				throw new ParseException();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "int":
			RightexprDatum = new integerDatum(RightExprvalue);
			break;
		case "date":
			RightexprDatum = new dateDatum(RightExprvalue);
			break;
		case "double":
			RightexprDatum = new doubleDatum(RightExprvalue);
			break;
		case "string":
			RightexprDatum = new stringDatum(RightExprvalue);
			break;
		}
		
		if (LeftExprValue.compareTo(RightexprDatum) != 0) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}
	}

	@Override
	public void visit(IsNullExpression expr) {
		IDatum LeftExprValue = null;
		
		if(expr.getLeftExpression() instanceof BinaryExpression)
		{
			expr.getLeftExpression().accept(this);
			LeftExprValue = colValue;
		}
		else if(expr.getLeftExpression() instanceof Column)
		{	
			Column Leftexpr=(Column) expr.getLeftExpression();
			String table_or_alias = null;
			String column_key = null;
			table_or_alias = Leftexpr.getTable().getName();
			
			if(table_or_alias == null)
			{
				column_key=columnToTable.get(Leftexpr.getColumnName())+"|"+Leftexpr.getColumnName();
			}
			else if(aliasToTable.get(table_or_alias)!=null)
			{
				column_key=aliasToTable.get(table_or_alias)+"|"+Leftexpr.getColumnName();
			}
			else
			{
				column_key=table_or_alias+"|"+Leftexpr.getColumnName();
			}
			
			LeftExprValue = myColumnDetails.get(column_key);
		}
	
		
		if (LeftExprValue == null) {
			resultFlag = true;
		} else {
			resultFlag = false;
		}
	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub

	}

}
