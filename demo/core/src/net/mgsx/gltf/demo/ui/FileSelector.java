package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.files.FileHandle;

public abstract class FileSelector {

  public FileHandle lastFile;

  /**
   * open a file
   */
  public abstract void open(Runnable runnable);

  /**
   * open a file
   */
  public abstract void save(Runnable runnable);

  /**
   * select a folder
   */
  public abstract void selectFolder(Runnable runnable);
}
