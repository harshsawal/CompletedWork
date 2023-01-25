package com.amica.billing;

public enum Terms {

	CASH(0),
	CREDIT_30(30),
	CREDIT_45(45),
	CREDIT_60(60),
	CREDIT_90(90);
	
	private int days;
	
	private Terms(int days) {
		this.days = days;
	}
	
	public int getDays() {
		return days;
	}
	
	public static Terms fromDays(int days) {
		for(Terms term : values()) {
			if(term.getDays() == days) {
				return term;
			}
		}
		return null;
	}
}
