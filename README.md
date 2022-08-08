# Poker Calculator

Poker Calculator is a JSF Icefaces Portlet designed for Liferay.
It supports Poker hand evaluation for up to seven players with known hole cards in Omaha, Omaha Hi/Lo and Texas Hold'em.

You can find a running version here: [https://evolutionsoft.ch/](https://evolutionsoft.ch/poker-calculator)

## License

Poker Calculator is provided under the GPL-3.0 or later GPL versions license.


## Known Issues

* FacesMessages Feedback about invalid user input stays as long as the page gets reloaded
* Redeploying a previously running portlet may need a Server Restart because of the shared libraries already loaded in another classpath


## Requirements

The distribution here is designed to run on linux like systems only. It provides prebuilt shared objects only for the used poker-eval C implementation.
You may need to rebuild the C library poker-eval for your local or server system.

The provided .so libraries in */lib* folder should work on a x64 like linux system.


## Installation Requirements

The provided release packages require *libpoker-eval.so* and *libpokerjni.so* to be present under */opt/poker-calculator/lib/*.

You can change the default library lookup path by rebuilding the Java sources with mvn. You can adapt *library.properties* under *src/main/resources* before the mvn build to use your custom library location.

The portlet is deployable without the native C libraries setup correctly. A calculation of given hands will not be possible before the libraries are loaded successfully. 

## Implementation Details

### Backend Calculator Dependency

Poker Calculator uses the poker-eval engine supporting even more Poker variations than implemented by Poker Calculator: (https://github.com/atinm/poker-eval)

### Build poker-eval for Java

I've found an old build of poker-eval providing *pokersource.jar*, *libpoker-eval.so* and *libpokerjni.so* on my PC.
However I can't give the details to get the three artifacts from the source no more at the moment.

### Frontend Icefaces portlet

The frontend uses Icefaces 4.3 as JSF implementation.

### Release package

The distributed war files in the releases here do not contain the pom.xml project version. This helps to get the same portlet context 'poker-calculator' for all versions and helps replacing an earlier deployed portlet war file.
