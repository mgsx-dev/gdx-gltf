package net.mgsx.gltf.ibl.io;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class AWTFileSelector extends FileSelector
{
	// TODO replace this native interface either by attaching to LibGDX applet or pure LibGDX browser ...

	public String path = ".";
	
	public AWTFileSelector() {
		super();
	}
	
	@Override
	public void open(Consumer<FileHandle> handler) {
		openDialog(handler, false);
	}
	
	private void openDialog(Consumer<FileHandle> handler, boolean save){
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
					handler.accept(lastFile);
				}
			});
		}else{
			// callback.cancel();
		}
		applet.destroy();
	}

	@Override
	public void selectFolder(Consumer<FileHandle> handler) {
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
					handler.accept(lastFile);
				}
			});
		}else{
			// callback.cancel();
		}
		applet.destroy();
		
	}

	@Override
	public void save(Consumer<FileHandle> handler) {
		openDialog(handler, true);
	}
}