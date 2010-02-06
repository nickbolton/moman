package net.deuce.moman;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.validator.routines.CurrencyValidator;
import org.apache.commons.validator.routines.PercentValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class Constants {

	public static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	public static final CurrencyValidator CURRENCY_VALIDATOR = new CurrencyValidator(false, true);
	public static final PercentValidator PERCENT_VALIDATOR = new PercentValidator(false);
	public static final Font STANDARD_FONT = new Font(Display.getCurrent(), new FontData[]{new FontData("Lucida Grande", 14, SWT.NORMAL)});
	public static final Font CHART_FONT = new Font(Display.getCurrent(), new FontData[]{new FontData("Lucida Grande", 12, SWT.NORMAL)});
	
}
