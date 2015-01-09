package edu.buffalo.cse562;

public class stringDatum implements IDatum{
	String s;
	public stringDatum(String s){
		this.s = s;
	}
	@Override
	public boolean equals(IDatum d) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public int compareTo(IDatum rightexprDatum) {
		String a = (String) this.getValue();
		String b = (String) rightexprDatum.getValue();
		int result = a.compareToIgnoreCase(b);
		return result;
	}
	@Override
	public Object getValue() {
		return this.s;
	}
}
