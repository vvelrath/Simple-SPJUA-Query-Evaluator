package edu.buffalo.cse562;

public interface IDatum {

	public boolean equals(IDatum d);

	public int compareTo(IDatum rightexprDatum);
	
	public Object getValue();

}
