package edu.buffalo.cse562;

public class booleanDatum implements IDatum{

	Boolean bool;
	public booleanDatum(String s) {
		    this.bool = Boolean.valueOf(s); 
	}
	@Override
	public boolean equals(IDatum d) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public int compareTo(IDatum rightexprDatum) {
		return 0;
	}
	@Override
	public Object getValue() {
		return this.bool;
	}
}
