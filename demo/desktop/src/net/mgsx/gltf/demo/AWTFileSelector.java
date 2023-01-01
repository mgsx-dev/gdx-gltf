package net.mgsx.gltf.demo;

import com.badlogic.gdx.Gdx;
import net.mgsx.gltf.demo.ui.FileSelector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class AWTFileSelector extends FileSelector {
  public String path = ".";

  public AWTFileSelector() {
    super();
  }

  @Override
  public void open(Runnable runnable) {
    openDialog(runnable, false, false);
  }

  @Override
  public void save(Runnable runnable) {
    openDialog(runnable, false, true);
  }

  @Override
  public void selectFolder(final Runnable callback) {
    openDialog(callback, true, false);
  }

  private void openDialog(final Runnable callback, boolean folderOnly, boolean save) {
    final JFileChooser fc = new JFileChooser(new File(path));

    JFrame f = new JFrame();
    f.setVisible(true);
    f.toFront();
    f.setVisible(false);
    f.setAlwaysOnTop(true);

    if (folderOnly) {
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
    if (r == JFileChooser.APPROVE_OPTION) {
      final File file = fc.getSelectedFile();
      path = folderOnly ? file.getPath() : file.getParent();
      Gdx.app.postRunnable(new Runnable() {
        @Override
        public void run() {
          lastFile = Gdx.files.absolute(file.getAbsolutePath());
          callback.run();
        }
      });
    }
    f.dispose();
  }
}