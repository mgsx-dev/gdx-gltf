package net.mgsx.gltf.demo;

import java.io.File;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.badlogic.gdx.Gdx;

import net.mgsx.gltf.demo.ui.FileSelector;

public class AWTFileSelector extends FileSelector
{
	// TODO replace this native interface either by attaching to LibGDX applet or pure LibGDX browser ...

	public String path = ".";
	
	public AWTFileSelector() {
		super();
	}
	
	@Override
	public void open(Runnable runnable) {
		openDialog(runnable, false);
	}
	
	private void openDialog(final Runnable callback, boolean save){
		JApplet applet = new JApplet(); // TODO fail safe
		final JFileChooser fc = new JFileChooser(new File(path));
		/*
		fc.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return callback.description();
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || callback.match(Gdx.files.absolute(f.getAbsolutePath()));
			}
		});
		*/
		int r = save ? fc.showSaveDialog(applet) : fc.showOpenDialog(applet);
		if(r == JFileChooser.APPROVE_OPTION){
			final File file = fc.getSelectedFile();
			path = file.getParent();
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					lastFile = Gdx.files.absolute(file.getAbsolutePath());
					callback.run();
				}
			});
		}else{
			// callback.cancel();
		}
		applet.destroy();
	}

	@Override
	public void selectFolder(final Runnable callback) {
		final boolean save = false;
		JApplet applet = new JApplet(); // TODO fail safe
		final JFileChooser fc = new JFileChooser(new File(path));
		fc.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Folder";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = save ? fc.showSaveDialog(applet) : fc.showOpenDialog(applet);
		if(r == JFileChooser.APPROVE_OPTION){
			final File file = fc.getSelectedFile();
			path = file.getPath();
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					lastFile = Gdx.files.absolute(file.getAbsolutePath());
					callback.run();
				}
			});
		}else{
			// callback.cancel();
		}
		applet.destroy();
		
	}

	@Override
	public void save(Runnable runnable) {
		openDialog(runnable, true);
	}
}