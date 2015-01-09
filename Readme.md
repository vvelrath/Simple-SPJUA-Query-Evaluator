Simple SQL Query Evaluator 
==============================

### Team members

Vivekanandh Vel Rathinam (vvelrath@buffalo.edu), 
Amitha narasimha Murthy (amithana@buffalo.edu)
Neeti Narayan (neetinar@buffalo.edu)

### Description
 
In this project, we implemented a simple SQL query evaluator with support for
Select, Project, Join, Union, and Aggregate operations. That is, the code will receive a
SQL file with a set of CREATE TABLE statements defining a schema for the data, and one
or more SELECT statements. More precisely, this query Evaluator is expected to evaluate 
the SELECT statements on provided data, and produce output in a standardized form.

### Parser

A parser converts a human-readable string into a structured representation of the program
(or query) that the string describes. An open-source SQL parser (JSQLParser) has been used
in this project. Documentation on how to use this parser is available at
http://jsqlparser.sourceforge.net

### Schema

The data directory contains files named table name.dat where table name
is the name used in a CREATE TABLE statement. Notice the effect of CREATE TABLE statements
is not to create a new file, but simply to link the given schema(tpch_schemas.sql) to an existing .dat
file. These files use vertical-pipe (’|’) as a field delimiter, and newlines (’\n’) as record
delimiters.

### Program execution

Run this program use the following syntax:

	java -cp build:jsqlparser.jar edu.buffalo.cse562.Main [--data data_directory] tpch_schemas.sql query.sql
	
	• data directory: A path to a directory containing data for this test. For
	each CREATE TABLE table name statement, there is a corresponding table name.dat
	in the data directory.
	• tpch_schemas.sql: Schema File for the tables present in the data directory
	• query.sql: One or more sql files for us to parse and evaluate.
	
	Example:
	$> ls data
	R.dat
	S.dat
	T.dat
	$> cat R.dat
	1|1|5
	1|2|6
	2|3|7
	$> cat query.sql
	CREATE TABLE R(A int, B int)
	SELECT B, C FROM R WHERE A = 1
	$> java -cp build:jsqlparser.jar edu.buffalo.cse562.Main --data data tpch_schemas.sql query.sql
	1|5
	2|6
	
