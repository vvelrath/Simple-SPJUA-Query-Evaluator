package edu.buffalo.cse562;

public class doubleDatum implements IDatum{

	Double d;
	public doubleDatum(String s){
		d = Double.parseDouble(s);
	}
		@Override
		public boolean equals(IDatum d) {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public int compareTo(IDatum rightexprDatum) {
			Double a = (Double) this.getValue();
			Double b = (Double) rightexprDatum.getValue();
			if (a>b) return 1;
			else if(a<b) return -1;
			else return 0;
		} 
		@Override
		public Object getValue() {
			return this.d;
		}

}
