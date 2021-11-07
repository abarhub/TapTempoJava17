package org.github.abarhub.taptempojava;

public class Version {
    public final static String CURRENT_VERSION = Version.class.getPackage().getImplementationVersion();

    public static String getVersion() {
        if (CURRENT_VERSION == null) {
            return "1.0.0-SNAPSHOT";
        } else {
            return CURRENT_VERSION;
        }
    }
}
