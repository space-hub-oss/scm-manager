package com.github.wisebrains.scm.build;

import com.github.wisebrains.scm.build.stream.StreamGobbler;
import org.apache.commons.cli.CommandLine;
import org.apache.maven.scm.ScmFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Build handler that decorates a {@link BuildStrategy} by creating the build process
 * and redirecting if necessary process output / error.
 *
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class BuildHandler {

  /** build strategy */
  private final BuildStrategy strategy;

  public BuildHandler( BuildStrategy strategy ) {
    this.strategy = strategy;
  }

  public void build( Collection< ScmFile > files , File workingDirectory , CommandLine commandLine )
      throws BuildException {
    try {
      Process process = strategy.buildProcess( files, workingDirectory, commandLine );
      new Thread( new StreamGobbler( process.getInputStream(), System.out, "[OUTPUT]" ) ).start();
      new Thread( new StreamGobbler( process.getErrorStream(), System.err, "[ERROR]" ) ).start();
      process.waitFor();
      if ( process.exitValue() != 0 ) throw new BuildException( workingDirectory, files );
    } catch ( IOException | InterruptedException e ) {
      throw new BuildException( e, workingDirectory, files );
    }
  }

}
