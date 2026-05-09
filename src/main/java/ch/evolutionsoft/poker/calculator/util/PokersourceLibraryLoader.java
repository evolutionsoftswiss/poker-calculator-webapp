package ch.evolutionsoft.poker.calculator.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class PokersourceLibraryLoader {

	static boolean initialized = false;

	public static synchronized void init() {

		if (!initialized) {

			Properties libraryProperties = new Properties();
			String absolutePath = "";
			try {
				libraryProperties.load(PokersourceLibraryLoader.class.getResourceAsStream("/library.properties"));

				String libraryBasePath = libraryProperties.getProperty("libraryPath");
	      
	      absolutePath = getAbsolutePath(libraryBasePath);

	      System.load(absolutePath + "libpoker-eval.so");
	      System.load(absolutePath + "libpokerjni.so");

			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}

			initialized = true;
		}
	}

	private static String getAbsolutePath(String libraryBasePath) {
	  
	  return new File(libraryBasePath).getAbsolutePath() + File.separator;
	}
	
  private PokersourceLibraryLoader() {
    // Empty private Constructor
  }
}
