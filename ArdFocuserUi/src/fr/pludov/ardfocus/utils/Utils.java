package fr.pludov.ardfocus.utils;

import java.io.File;

public class Utils {

	public static File getApplicationSettingsFolder(String application)
	{
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();
		//to determine what the workingDirectory is.
		//if it is some version of Windows
		if (OS.contains("WIN"))
		{
		    //it is simply the location of the "AppData" folder
		    workingDirectory = System.getenv("AppData");
		}
		//Otherwise, we assume Linux or Mac
		else
		{
		    //in either case, we would start in the user's home directory
		    workingDirectory = System.getProperty("user.home");
		    //if we are on a Mac, we are not done, we look for "Application Support"
		    // workingDirectory += "/Library/Application Support";
		}
		
		File result = new File(workingDirectory);
		result = new File(result, "." + application);
		return result;
	}
}
