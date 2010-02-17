package net.deuce.moman.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.deuce.moman.Constants;

public class Utils {
	
	private static Pattern currencyPattern;
	private static NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
	private static NumberFormat DOUBLE_FORMAT;
	
	static {
		DOUBLE_FORMAT = NumberFormat.getNumberInstance();
		DOUBLE_FORMAT.setMaximumFractionDigits(2);
		DOUBLE_FORMAT.setGroupingUsed(false);
		
		currencyPattern = Pattern.compile("^[0-9$()-,.]+$");
	}

	public static boolean validateCurrency(String value) {
		
		Matcher m = currencyPattern.matcher(value);
		if (!m.find()) return false;
		
		if (Constants.CURRENCY_VALIDATOR.isValid(value)) {
			return true;
		}
		try {
			Number number = CURRENCY_FORMAT.parse(value);
			return true;
		} catch (ParseException e) {
		}
		return false;
	}

	public static Double parseCurrency(String value) {
		if (Constants.CURRENCY_VALIDATOR.isValid(value)) {
			return Constants.CURRENCY_VALIDATOR.validate(value).doubleValue();
		}
		try {
			return CURRENCY_FORMAT.parse(value).doubleValue();
		} catch (ParseException e) {
		}
		return null;
	}
	
	public static String formatDouble(Double value) {
		if (value == null) return null;
		return DOUBLE_FORMAT.format(value);
	}
	
	public static double round(double value) {
		return Math.round(value*100.0)/100.0;
	}
}
