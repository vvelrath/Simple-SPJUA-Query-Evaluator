package edu.buffalo.cse562;

public class integerDatum implements IDatum{

	int i;
	public integerDatum(String s){
		i = Integer.parseInt(s);
	}
	@Override
	public Object getValue(){
		return this.i;
	}
	@Override
	public boolean equals(IDatum d) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public int compareTo(IDatum rightexprDatum) {
		int a = (Integer) this.getValue();
		int b = (Integer) rightexprDatum.getValue();
		if (a > b)
			return 1;
		else if (a < b)
			return -1;
		else 
			return 0;
	}

}
