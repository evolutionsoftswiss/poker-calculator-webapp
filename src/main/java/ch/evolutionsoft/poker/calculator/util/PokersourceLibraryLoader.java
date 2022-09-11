package ch.evolutionsoft.poker.calculator.util;

import java.io.IOException;
import java.util.Properties;

public class PokersourceLibraryLoader {

	static boolean initialized = false;

	public static synchronized void init() {

		if (!initialized) {

			Properties libraryProperties = new Properties();
			try {
				libraryProperties.load(PokersourceLibraryLoader.class.getResourceAsStream("/library.properties"));

	      String libraryBasePath = libraryProperties.getProperty("libraryPath");

	      System.load(libraryBasePath + "libpoker-eval.so");
	      System.load(libraryBasePath + "libpokerjni.so");

			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}

			initialized = true;
		}
	}
  
  private PokersourceLibraryLoader() {
    // Empty private Constructor
  }
}
