package edu.buffalo.cse562;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class dateDatum implements IDatum{
	String date;
	public dateDatum(String s) {
		date = s;
	}
	
	@Override
	public boolean equals(IDatum d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int compareTo(IDatum rightexprDatum) {
		Date a = null,b = null;
		String aString = (String) this.getValue();
		String bString = (String) rightexprDatum.getValue();
		try {
		a = new SimpleDateFormat("yyyy-MM-dd").parse(aString);
		b = new SimpleDateFormat("yyyy-MM-dd").parse(bString);
		} catch(ParseException ex) {
			ex.printStackTrace();
		}
		if (a.after(b)) return 1;
		else if (a.before(b)) return -1;
		else return 0;
	}

	@Override
	public Object getValue() {
		return this.date;
	}

}
