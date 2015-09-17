package com.github.wisebrains.scm.build.recovery;

import org.apache.commons.cli.CommandLine;
import org.apache.maven.scm.ScmFile;

import java.io.File;
import java.util.List;

/**
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public interface BuildRecovery {

  /**
   * Backs up the files that were to be updated.
   *
   * <p>The backed up files will fetched later following the same strategy they were
   * backed with and recovered by calling {@link #recover}.
   *
   * @param files files which failed to be built
   * @param workingDirectory maven project directory
   */
  void backup( List< ScmFile > files, File workingDirectory );

  /**
   * Recovers the backed up files essentially by performing a build again.
   *
   * @param workingDirectory maven project directory
   * @param cmd              commandline used to launch the recovery
   */
  void recover( File workingDirectory, CommandLine cmd);

}
