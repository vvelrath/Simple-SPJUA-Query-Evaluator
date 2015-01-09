package edu.buffalo.cse562;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class AggregateOperator implements IOperator {
	IOperator input;
	List<?> groupByCols;
	List<?> selectCols;
	StringBuilder grpByCompositeKey;
	Column[] schema;
	PlainSelect pselect;

	public static HashMap<String, IDatum[]> myAggMap;
	public static List<IDatum[]> tuples = new ArrayList<IDatum[]>();
	private static int k = 0;

	private boolean evaluated = false;

	/*
	 * public StringBuilder grpByKeyName = new StringBuilder(); public String
	 * grpbyKName = null;
	 */
	AggregateCalculator aggCalc;

	public AggregateOperator(IOperator input, Column[] schema,
			PlainSelect pselect) {
		this.input = input;
		this.groupByCols = pselect.getGroupByColumnReferences();
		this.schema = schema;
		this.grpByCompositeKey = null;
		this.pselect = pselect;
		selectCols = pselect.getSelectItems();
		myAggMap = new HashMap<String, IDatum[]>();
		aggCalc = new AggregateCalculator();

	}

	/*
	 * public void InitializeHashMapSchema(StatementEvaluator evaluator) { //
	 * populate the first row of the HashMap List<?> groupByCols =
	 * pselect.getGroupByColumnReferences();
	 * 
	 * // Parse the group by columns and construct key name, if they exist if
	 * (checkForGroupBy()) { for (int i = 0; i < groupByCols.size(); i++) {
	 * String colName = groupByCols.get(i).toString();
	 * 
	 * if (i == groupByCols.size() - 1) {
	 * 
	 * grpByKeyName.append(colName);
	 * 
	 * } else { grpByKeyName.append(colName + "|"); } }
	 * 
	 * grpbyKName = grpByKeyName.toString(); } else { grpbyKName = "None"; }
	 * 
	 * }
	 */

	public boolean IsGroupBy() {
		boolean groupByFlag = false;

		if (groupByCols != null) {
			groupByFlag = true;

		}
		return groupByFlag;
	}

	public void constructKey(IDatum[] colValues, StatementEvaluator evaluator,
			Column[] schema) {

		grpByCompositeKey = new StringBuilder();

		if (!IsGroupBy()){
			grpByCompositeKey.append("None");
		}
		
		else
		{
			for (int i = 0; i < groupByCols.size(); i++) {
				/*String colName = groupByCols.get(i).toString();

				IDatum val = evaluator.wasMyColumnDetails.get(colName);*/				
								
				
				Column col = (Column) groupByCols.get(i);
                String table_or_alias = col.getTable().getName();
                String column_key = null;
                
                if(table_or_alias == null)
                {
                    column_key=evaluator.columnToTable.get(col.getColumnName())+"|"+col.getColumnName();
                }
                else if(evaluator.aliasToTable.get(table_or_alias)!=null)
                {
                    column_key=evaluator.aliasToTable.get(table_or_alias)+"|"+col.getColumnName();
                }
                else
                {
                    column_key=table_or_alias+"|"+col.getColumnName();
                }
                IDatum val = evaluator.myColumnDetails.get(column_key);
				
				
				
				if(val!=null){
				
				Object obj = val.getValue();
				String value = obj.toString();

				if (i == groupByCols.size() - 1) {

					grpByCompositeKey.append(value);

				} else {
					String s=value+"|";
					grpByCompositeKey.append(s);
				}
				}
			}
		} 

	}

	public int EvaluateAggregation() {
		IDatum[] colValues = null;

		do {
			colValues = input.readOneTuple();

			
			if (colValues != null) {
				StatementEvaluator evaluator = new StatementEvaluator(input.getSchema()/*schema*/,
						colValues);
					constructKey(colValues, evaluator, input.getSchema()/*schema*/);
					// InitializeHashMapSchema(evaluator);
				populateHashmap(colValues, evaluator);
			}

		} while (colValues != null);
		for (String key : myAggMap.keySet()) {
			IDatum[] aggrTuple = myAggMap.get(key);
			tuples.add(aggrTuple);
		}
		evaluated = true;
		return tuples.size();
	}

	

	public void populateHashmap(IDatum[] colValues, StatementEvaluator evaluator) {

		IDatum[] ColumnsInSelect = new IDatum[selectCols.size()];

		String key = grpByCompositeKey.toString();
		int m=0;
		for (int j = 0; j < selectCols.size(); j++) {

			
			SelectExpressionItem sEItem = (SelectExpressionItem) selectCols.get(j);
            Expression expr = sEItem.getExpression();
            
            if(expr instanceof Column)
            {    
                Column col = (Column) expr;
                String table_or_alias = col.getTable().getName();
                String column_key = null;
                
                if(table_or_alias == null)
                {
                    column_key=evaluator.columnToTable.get(col.getColumnName())+"|"+col.getColumnName();
                }
                else if(evaluator.aliasToTable.get(table_or_alias)!=null)
                {
                    column_key=evaluator.aliasToTable.get(table_or_alias)+"|"+col.getColumnName();
                }
                else
                {
                    column_key=table_or_alias+"|"+col.getColumnName();
                }
                IDatum colValue = evaluator.myColumnDetails.get(column_key);
                
                ColumnsInSelect[j] = colValue;
            }
			
			/*if (evaluator.wasMyColumnDetails.containsKey(selectCols.get(j)
					.toString())) {
				SelectExpressionItem sEItem = (SelectExpressionItem) selectCols
						.get(j);
				Expression expr = sEItem.getExpression();
				Column acol = (Column) expr;
				IDatum colValue = evaluator.wasMyColumnDetails.get(acol
						.getColumnName());
				ColumnsInSelect[j] = colValue;
			}*/

			else {
				// it is an aggregate function
				// passing the expression, old value, key and evaluator(to
				// extract column name)

				/*SelectExpressionItem sEItem = (SelectExpressionItem) selectCols
						.get(j);*/
				String aggColName=null;
				/*Expression expr = sEItem.getExpression();*/
				if (expr instanceof Function) {
					// visits Function and initializes the column on which the
					// aggregate is called
					expr.accept(evaluator);
					// evaluate the expression inside aggregate and store it in hash map with its alias
					if(sEItem.getAlias()!=null){
						aggColName=sEItem.getAlias();
					evaluator.wasMyColumnDetails.put(aggColName,evaluator.getColValue());
					
					FromScanner.colDetails.put(aggColName,evaluator.getType());
					}
					else if(evaluator.AggOnColumn!=null){
						aggColName=evaluator.AggOnColumn.getColumnName();
						}
					else {
						sEItem.setAlias("temp"+m);
						aggColName=sEItem.getAlias();
						evaluator.wasMyColumnDetails.put(aggColName,evaluator.getColValue());
						FromScanner.colDetails.put(aggColName,evaluator.getType());
						m++;
					}
					
					
					if (myAggMap.get(key) != null) {
						// get the existing values to update only in case of an
						// existing key
						ColumnsInSelect = myAggMap.get(key);
					}
					ColumnsInSelect[j] = aggCalc.ComputeAggregate(expr,
							ColumnsInSelect[j], evaluator, key,aggColName );

				}

			}

		}
		myAggMap.put(key, ColumnsInSelect);
	}

	@Override
	public void resetStream() {
		input.resetStream();

	}

	@Override
	public IDatum[] readOneTuple() {
		if (!evaluated) {
			k = EvaluateAggregation();
		}
		IDatum[] tuple = null;
		if (k > 0) {
			tuple = tuples.get(--k);
		}

		return tuple;

	}

	@Override
	public Column[] getSchema() {
		// TODO Auto-generated method stub
		return input.getSchema();
	}

	@Override
	public void setSchema(Column[] col) {
		this.schema = schema;

	}

}
