package net.mgsx.gltf.demo.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

import net.mgsx.gltf.scene3d.utils.IBLBuilder;

// TODO allow to edit and export!
public class IBLStudio {

	public static abstract class IBLPreset {
		public final String name;
		
		public IBLPreset(String name) {
			super();
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		abstract public IBLBuilder createBuilder(DirectionalLight sun);
	}
	
	public static final IBLPreset defaultPreset = new IBLPreset("default") {
		@Override
		public IBLBuilder createBuilder(DirectionalLight sun) {
			return null;
		}
	};
	public static final IBLPreset outdoor = new IBLPreset("outdoor") {
		@Override
		public IBLBuilder createBuilder(DirectionalLight sun) {
			return IBLBuilder.createOutdoor(sun);
		}
	};
	public static final IBLPreset indoor = new IBLPreset("indoor") {
		@Override
		public IBLBuilder createBuilder(DirectionalLight sun) {
			return IBLBuilder.createIndoor(sun);
		}
	};
	public static final IBLPreset space = new IBLPreset("space") {
		@Override
		public IBLBuilder createBuilder(DirectionalLight sun) {
			IBLBuilder ibl = IBLBuilder.createCustom(sun);
			ibl.lights.first().exponent = 100f;
			ibl.lights.first().color.set(Color.WHITE);
			return ibl;
		}
	};
	
	public static final IBLPreset [] presets = {
		defaultPreset, outdoor, indoor, space
	};
}
