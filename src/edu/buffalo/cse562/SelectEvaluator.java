package edu.buffalo.cse562;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;

public class SelectEvaluator implements SelectVisitor {
	public IOperator operator = null;
	File dataDir = null;
	HashMap<String, CreateTable> tables = new HashMap<String, CreateTable>();

	public static boolean orderByFlag;
	public static boolean aggrFlag;

	StatementEvaluator evaluator = new StatementEvaluator();

	public SelectEvaluator(IOperator operator, File dataDir,
			HashMap<String, CreateTable> tables) {

		this.operator = operator;
		this.dataDir = dataDir;
		this.tables = tables;
		// TODO Auto-generated constructor stub
	}

	public Expression getOnExpression(PlainSelect pselect,
			ArrayList<String> tablesBeingJoined, ArrayList<String> conditions) {
		ArrayList<String> evaluatedConditions = new ArrayList<String>(conditions);

		Expression onExpr = null;
		StringBuilder tempExpr = new StringBuilder();

		ArrayList<String> tablesNotBeingJoined = new ArrayList<String>();
		List<?> joins = pselect.getJoins();
		for (int i = 0; i < joins.size(); i++) {
			if (!(tablesBeingJoined.contains(joins.get(i).toString()))) {

				tablesNotBeingJoined.add(joins.get(i).toString());
			}
		}

		for (int i = 0; i < tablesNotBeingJoined.size(); i++) {
			for (int j = 0; j < evaluatedConditions.size(); j++) {
				if (evaluatedConditions.get(j).contains(
						tablesNotBeingJoined.get(i))) {
					evaluatedConditions.remove(j);
					--j;

				}
			}
		}

		for (int i = 0; i < evaluatedConditions.size(); i++) {
			if (i == evaluatedConditions.size() - 1) {
				tempExpr.append(evaluatedConditions.get(i));
			} else {
				// TODO: extend for "OR"
				tempExpr.append(" AND ");
			}
		}

		StringReader strReader = new StringReader(tempExpr.toString());
		CCJSqlParser parser = new CCJSqlParser(strReader);
		try {
			onExpr = parser.Expression();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int k = 0; k < conditions.size(); k++) {
			if (evaluatedConditions.contains(conditions.get(k))) {
				conditions.remove(k);
				--k;
			}
		}

		return onExpr;
	}

	@Override
	public void visit(PlainSelect pselect) {
		
		FromScanner fromScan = new FromScanner(dataDir, tables);
		pselect.getFromItem().accept(fromScan);
		Column[] schema = null;
		ArrayList<String> whereconditions = new ArrayList<String>();
		operator = fromScan.source;
		
		if (pselect.getWhere() != null) {
			String whereCondition = pselect.getWhere().toString();

			Scanner sc = new Scanner(whereCondition);
			sc.useDelimiter("AND");

			while (sc.hasNext()) {
				whereconditions.add(sc.next());
			}
			sc.close();
		}

		ArrayList<String> tablesBeingJoined = new ArrayList<String>();
		Column[] leftschema = operator.getSchema();
		String leftTableName = leftschema[0].getTable().getName();

		tablesBeingJoined.add(leftTableName);

		if (pselect.getJoins() != null) {
			List<?> joins = pselect.getJoins();
			for (int i = 0; i < joins.size(); i++) {

				Join rightjoin = (Join) joins.get(i);

				String rightTableName = rightjoin.toString();
				tablesBeingJoined.add(rightTableName);
				Expression onExpression = getOnExpression(pselect,
						tablesBeingJoined, whereconditions);

				rightjoin.getRightItem().accept(fromScan);
				IOperator rightjoinoperator = fromScan.source;
				schema = fromScan.schema;

				operator = new JoinOperator(operator, rightjoinoperator,
						onExpression, schema);
			}
		}

		if (pselect.getWhere() != null && pselect.getJoins() == null) {
			operator = new selectOperator(operator, operator.getSchema(),
					pselect.getWhere());
		}

		for (int j = 0; j < pselect.getSelectItems().size(); j++) {
			SelectExpressionItem sEItem = (SelectExpressionItem) pselect
					.getSelectItems().get(j);
			Expression expr = sEItem.getExpression();

			expr.accept(evaluator);
			if (StatementEvaluator.getAggFuncFlag()) {
				break;
			}
		}

		// GROUP BY and AGGREGATES
		if (StatementEvaluator.getAggFuncFlag()) {
			aggrFlag = true;
			operator = new AggregateOperator(operator, operator.getSchema(),
					pselect);
			// operator.readOneTuple();
		}

		// ORDER BY clause
		if ((pselect.getOrderByElements() != null)) {
			orderByFlag = true;
			operator = new OrderByOperator(operator, operator.getSchema(),
					pselect);
		}
		// end of ORDER BY evaluation

		// Go to project only if Aggregate operator is not present, otherwise
		// aggregate operator handles everything
		if (pselect.getSelectItems() != null
				&& StatementEvaluator.getAggFuncFlag() == false
				&& orderByFlag == false) {
			operator = new projectOperator(operator, operator.getSchema(),
					pselect.getSelectItems());
		}
	}

	@Override
	public void visit(Union arg0) {
		// TODO Auto-generated method stub

	}
}
