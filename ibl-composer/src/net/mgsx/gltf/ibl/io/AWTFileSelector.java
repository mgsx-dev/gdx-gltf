package net.mgsx.gltf.ibl.io;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class AWTFileSelector extends FileSelector
{
	public String path = ".";
	
	public AWTFileSelector() {
		super();
	}
	
	@Override
	public void open(Consumer<FileHandle> handler) {
		openDialog(handler, false, false);
	}
	
	@Override
	public void save(Consumer<FileHandle> handler) {
		openDialog(handler, false, true);
	}
	
	@Override
	public void selectFolder(Consumer<FileHandle> handler) {
		openDialog(handler, true, false);
	}
	
	private void openDialog(Consumer<FileHandle> handler, boolean folderOnly, boolean save){
		final JFileChooser fc = new JFileChooser(new File(path));
		
		JFrame f = new JFrame();
        f.setVisible(true);
        f.toFront();
        f.setVisible(false);
        f.setAlwaysOnTop (true);
        
        if(folderOnly){
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
        }
		
		int r = save ? fc.showSaveDialog(f) : fc.showOpenDialog(f);
		if(r == JFileChooser.APPROVE_OPTION){
			final File file = fc.getSelectedFile();
			path = folderOnly ? file.getPath() : file.getParent();
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					lastFile = Gdx.files.absolute(file.getAbsolutePath());
					handler.accept(lastFile);
				}
			});
		}
		f.dispose();
	}
}