package net.mgsx.gltf.constant;

import static java.lang.String.format;

public class CommonConstants {

  public static final String EOL = System.lineSeparator();

  private CommonConstants() {
    throw new IllegalStateException(format("Cannot create instance of %s", getClass()));
  }
}
