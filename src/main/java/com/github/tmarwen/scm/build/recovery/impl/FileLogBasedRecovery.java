package com.github.tmarwen.scm.build.recovery.impl;

import com.github.tmarwen.scm.build.BuildException;
import com.github.tmarwen.scm.build.BuildHandler;
import com.github.tmarwen.scm.build.BuildStrategy;
import com.github.tmarwen.scm.build.recovery.BuildRecovery;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class FileLogBasedRecovery implements BuildRecovery {

  private static final String FILE_NAME = "build_recovery.log";

  private final Logger logger;

  @Inject
  public FileLogBasedRecovery( Logger logger ) {
    this.logger = logger;
  }

  @Override
  public void backup( List< ScmFile > files, File workingDirectory ) {
    try {
      File logFile = new File( workingDirectory, FILE_NAME );
      if ( !logFile.exists() && logFile.createNewFile() ) {
        Collection< String > filePaths = toFilePaths( files );
        Files.write( logFile.toPath(), filePaths, Charset.defaultCharset(), StandardOpenOption.WRITE );
      }
    } catch ( IOException e ) {
      logger.warning( "Failed to backup the build failure." );
    }
  }

  @Override
  public void recover( File workingDirectory, CommandLine cmd ) {
    File logFile = new File( workingDirectory, FILE_NAME );
    if ( logFile.exists() ) {
      try {
        List< String > fileNames = Files.readAllLines( logFile.toPath(), Charset.defaultCharset() );
        Collection< ScmFile > scmFiles = toFiles( fileNames );
        BuildStrategy strategy = BuildStrategy.resolveStrategy( scmFiles );
        BuildHandler buildHandler = new BuildHandler( strategy );
        buildHandler.build( scmFiles, workingDirectory, cmd );
        logFile.delete();
      } catch ( IOException e ) {
        logger.warning( "Failed to recover the build." );
      } catch ( BuildException be ) { // recovery cannot recover itself
        logger.warning( "An error occurred while triggering the maven build:" +
            SystemUtils.LINE_SEPARATOR + be.getMessage() );
      }
    }
  }

  private Collection< String > toFilePaths( Collection< ScmFile > scmFiles) {
    return Collections2.transform( scmFiles, new Function< ScmFile, String > () {
      @Override
      public String apply( ScmFile scmFile ) {
        return scmFile.getPath();
      }
    } );
  }

  private Collection< ScmFile > toFiles( Collection< String > fileNames) {
    return Collections2.transform( fileNames, new Function< String, ScmFile >() {
      @Override
      public ScmFile apply( String s ) {
        return new ScmFile( s, ScmFileStatus.UNKNOWN );
      }
    } );
  }
}
