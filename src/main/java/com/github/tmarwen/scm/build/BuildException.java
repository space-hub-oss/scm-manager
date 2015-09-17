package com.github.tmarwen.scm.build;

import org.apache.maven.scm.ScmFile;

import java.io.File;
import java.util.Collection;

/**
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class BuildException extends Throwable {

  private final File buildDirectory;
  private final Collection< ScmFile > updatedScmFiles;

  public BuildException() {
    this.buildDirectory = null;
    this.updatedScmFiles = null;
  }

  public BuildException( File workingDirectory, Collection< ScmFile > files ) {
    this.buildDirectory = workingDirectory;
    this.updatedScmFiles = files;
  }

  public BuildException( Exception e, File workingDirectory, Collection< ScmFile > files ) {
    this.initCause( e );
    this.buildDirectory = workingDirectory;
    this.updatedScmFiles = files;
  }

  public File getBuildDirectory() {
    return buildDirectory;
  }

  public Collection< ScmFile > getUpdatedScmFiles() {
    return updatedScmFiles;
  }
}
