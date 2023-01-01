package net.mgsx.gltf.scene3d.utils;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;

import static java.lang.String.format;

public class LightUtils {

  public static class LightsInfo {

    public int dirLights = 0;
    public int pointLights = 0;
    public int spotLights = 0;
    public int miscLights = 0;

    public void reset() {
      dirLights = 0;
      pointLights = 0;
      spotLights = 0;
      miscLights = 0;
    }
  }

  public static LightsInfo getLightsInfo(LightsInfo info, Environment environment) {
    info.reset();
    DirectionalLightsAttribute dla = environment.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
    if (dla != null) {
      info.dirLights = dla.lights.size;
    }

    PointLightsAttribute pla = environment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
    if (pla != null) {
      info.pointLights = pla.lights.size;
    }

    SpotLightsAttribute sla = environment.get(SpotLightsAttribute.class, SpotLightsAttribute.Type);
    if (sla != null) {
      info.spotLights = sla.lights.size;
    }
    return info;
  }

  public static LightsInfo getLightsInfo(LightsInfo info, Iterable<BaseLight<?>> lights) {
    info.reset();
    for (BaseLight<?> light : lights) {
      if (light instanceof DirectionalLight) {
        info.dirLights++;
      } else if (light instanceof PointLight) {
        info.pointLights++;
      } else if (light instanceof SpotLight) {
        info.spotLights++;
      } else {
        info.miscLights++;
      }
    }
    return info;
  }

  private LightUtils() {
    throw new IllegalStateException(format("Cannot create instance of %s", getClass()));
  }
}
