package fr.pludov.ardfocuser.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.pludov.ardfocus.utils.Utils;

public class Logger {
	private static FileOutputStream fos;

	private static final SimpleDateFormat fitsFormat = new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss");
	
	static void reset()
	{
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fos = null;
		}
		File logPath = Utils.getApplicationSettingsFolder("ArdFocuser");
		logPath.mkdirs();
		
		try {
			fos = new FileOutputStream(new File(logPath, "log-" + fitsFormat.format(new Date()) + ".csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	static private OutputStream getOutputStream() throws IOException
	{
		if (fos == null) {
			throw new IOException("Log closed");
		}
		return fos;
	}
	
	static private void append(String content)
	{
		try {
			content += "\r\n";
			OutputStream outputStream = getOutputStream();
			outputStream.write(content.getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static private String encodeCsvField(String s)
	{
		if (s.contains(" ") || s.contains("\"") || s.contains(",")) {
			s = "\"" + s.replaceAll("\"", "\"\"") + "\"";
		}
		return s;
	}

	static private String formatDouble(Double d, int precision)
	{
		if (d == null) return "";
		if (precision > 0) {
			return String.format(Locale.US, "%." + precision + "f", d);
		} else {
			return String.format(Locale.US, "%.0f", d);
		}
	}
	
	static void log(String message)
	{
		append(formatDouble(System.currentTimeMillis() / 1000.0, 3) + ",LOG," + encodeCsvField(message));
	}
	
	static void stateUpdated(Focuser f)
	{
		append(formatDouble(System.currentTimeMillis() / 1000.0, 3) + ",STATUS,"
				+ encodeCsvField(Integer.toString(f.motorPosition))
				+ encodeCsvField(Integer.toString(f.motorState))
				+ encodeCsvField(formatDouble(f.scopeTemp, 2))
				+ encodeCsvField(formatDouble(f.extTemp, 2))
				+ encodeCsvField(formatDouble(f.extHum, 1))
				+ encodeCsvField(formatDouble(f.battery, 2))
				+ encodeCsvField(formatDouble(f.heater, 1))
				+ encodeCsvField(Integer.toString(f.filterWheelPosition))
				+ encodeCsvField(f.filterWheelState.name()));
	}
	
	private Logger() {
		// TODO Auto-generated constructor stub
	}

}
