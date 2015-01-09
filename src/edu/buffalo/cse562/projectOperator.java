package edu.buffalo.cse562;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

// class not implemented
public class projectOperator implements IOperator {

	IOperator input;
	List<?> selectItems;
	Column[] schema;
	HashMap<String, Integer> schema_map;
	IDatum[] colValues = null;
	Table[] tables;
	
	public HashMap<String,String> aliasToTable=new HashMap<String,String>();
	public HashMap<String,String> columnToTable=new HashMap<String,String>();
	public HashMap<String,Table> NameToTable=new HashMap<String,Table>();


	public projectOperator(IOperator input, Column[] schema, List<?> selectItems) {
		this.input = input;
		this.schema = input.getSchema();
		this.selectItems = selectItems;
		this.schema_map = new HashMap<String, Integer>();
		tables = new Table[selectItems.size()];
		
		for (int i = 0; i < schema.length; i++) {
			//schema_map.put(schema[i].getColumnName(), i);
			schema_map.put(schema[i].getTable().getName()+"|"+schema[i].getColumnName(), i);
			aliasToTable.put(schema[i].getTable().getAlias(), schema[i].getTable().getName());
			columnToTable.put(schema[i].getColumnName(), schema[i].getTable().getName());
			NameToTable.put(schema[i].getTable().getName(), schema[i].getTable());
			
		}

	}

	@Override
	public void resetStream() {
		// TODO Auto-generated method stub
		input.resetStream();
	}

	@Override
	public IDatum[] readOneTuple() {

		ArrayList<IDatum> tuple_list = new ArrayList<IDatum>();
		IDatum[] tuple = new IDatum[tuple_list.size()];
		
		do {
						colValues = input.readOneTuple();
				
				schema = input.getSchema();
				
				for (int i = 0; i < schema.length; i++) {
					//schema_map.put(schema[i].getColumnName(), i);
					schema_map.put(schema[i].getTable().getName()+"|"+schema[i].getColumnName(), i);
					aliasToTable.put(schema[i].getTable().getAlias(), schema[i].getTable().getName());
					columnToTable.put(schema[i].getColumnName(), schema[i].getTable().getName());
					NameToTable.put(schema[i].getTable().getName(), schema[i].getTable());
					
				}
				
			if (colValues == null) {
				// has reached EOF
				Column[] new_schema = new Column[selectItems.size()];
				for (int j = 0; j < selectItems.size(); j++)
				{
					SelectExpressionItem select_expr = (SelectExpressionItem) selectItems.get(j);
					if(select_expr.getAlias()!=null)
						new_schema[j] = new Column(tables[j],select_expr.getAlias());
					else
						new_schema[j] = new Column(tables[j],select_expr.getExpression().toString());
				}
			
				this.setSchema(new_schema);
				
				return null;
			}

			StatementEvaluator evaluator = new StatementEvaluator(input.getSchema(),colValues);
			
			for (int j = 0; j < selectItems.size(); j++) {
				SelectExpressionItem select_expr = (SelectExpressionItem) selectItems
						.get(j);
				Expression expr = select_expr.getExpression();

				if((expr instanceof BinaryExpression)||(expr instanceof Parenthesis))
				{	
					
					expr.accept(evaluator);
					tuple_list.add(evaluator.getColValue());
					tables[j] = NameToTable.get(evaluator.tableName);
				}
				else
				{	
					Column col = (Column) select_expr.getExpression();
					String col_name = col.getColumnName();
					String table_or_alias = col.getTable().getName();
					
					String column_key = null;
					String table_name = null;
					
					if(table_or_alias == null)
					{
						column_key=columnToTable.get(col_name)+"|"+col_name;
						table_name =columnToTable.get(col_name);
					}
					else if(aliasToTable.get(table_or_alias)!=null)
					{
						column_key=aliasToTable.get(table_or_alias)+"|"+col_name;
						table_name = aliasToTable.get(table_or_alias);
					}
					else
					{
						column_key=table_or_alias+"|"+col_name;
						table_name = table_or_alias;
					}
					
					tuple_list.add(colValues[schema_map.get(column_key)]);
					tables[j] = NameToTable.get(table_name);
				}
				
				

			}
			
		} while (tuple_list == null);
		
		

		Column[] new_schema = new Column[selectItems.size()];
		for (int j = 0; j < selectItems.size(); j++)
		{
			SelectExpressionItem select_expr = (SelectExpressionItem) selectItems.get(j);
			if(select_expr.getAlias()!=null)
				new_schema[j] = new Column(tables[j],select_expr.getAlias());
			else
				new_schema[j] = new Column(tables[j],select_expr.getExpression().toString());
		}
	
		this.setSchema(new_schema);
		
		
		tuple = tuple_list.toArray(tuple);
		return tuple;
	}
	
	@Override
	public Column[] getSchema() {
		// TODO Auto-generated method stub
		//return input.getSchema();
		return this.schema;
	}
	@Override
	public void setSchema(Column[] col) {
		// TODO Auto-generated method stub
		//input.setSchema(col);
		this.schema=col;
	}
}