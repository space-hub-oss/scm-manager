package com.github.wisebrains.scm.build;

import com.google.common.base.Joiner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.scm.ScmFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public enum BuildStrategy {

  SELECTIVE {

    private final Pattern srcPattern = Pattern.compile( "(\\\\src.*)" );
    private final Pattern moduleFullPathPattern = Pattern.compile( "(.*\\\\)" );

    @Override
    public Process buildProcess( Collection< ScmFile > files, File workingDir, CommandLine cmd ) throws IOException {
      // resolve modules to build
      Set< String > modules = new HashSet<>();
      for ( ScmFile file : files ) {
        modules.add( moduleFullPathPattern.matcher(
            srcPattern.matcher( file.getPath() ).replaceAll( StringUtils.EMPTY ) ).replaceAll( StringUtils.EMPTY ) );
      }

      // concatenate the modules as project arguments
      StringBuilder modulesSb = new StringBuilder( ":" );
      for ( Iterator< String > it = modules.iterator(); it.hasNext(); ) {
        modulesSb.append( it.next() );
        if ( it.hasNext() ) modulesSb.append( MVN_MODULE_SEPARATOR );
      }

      ProcessBuilder processBuilder = new ProcessBuilder(
          MVN_COMMAND,
          MVN_CLEAN_LIFECYCLE,
          MVN_INSTALL_LIFECYCLE,
          ( cmd != null && cmd.hasOption( "o" ) ) ? StringUtils.EMPTY : MVN_OFFLINE_OPTION,
          MVN_PROJECT_OPTION,
          modulesSb.toString())
          .directory( workingDir );
      logBuiltCommand(processBuilder);
      return processBuilder.start();
    }
  }, ALL {
    @Override
    public Process buildProcess( Collection< ScmFile > files, File workingDir, CommandLine cmd ) throws IOException {
      ProcessBuilder processBuilder = new ProcessBuilder( MVN_COMMAND, MVN_CLEAN_LIFECYCLE, MVN_INSTALL_LIFECYCLE,
          ( cmd != null && cmd.hasOption( "o" ) ) ? StringUtils.EMPTY : MVN_OFFLINE_OPTION )
          .directory( workingDir );
      logBuiltCommand( processBuilder );
      return processBuilder.start();
    }
  };

  private static void logBuiltCommand( ProcessBuilder processBuilder ) {
    LOGGER.info( Joiner.on( ' ' ).skipNulls().join( "Maven command", ":", processBuilder.command().toArray() ) );
  }

  static {
    String mvnHome = System.getProperty( "maven.home" );
    if ( StringUtils.isEmpty( mvnHome ) ) {
      mvnHome = System.getenv().get( "M2_HOME" );
    }
    MVN_COMMAND = mvnHome + File.separatorChar + "bin" + File.separatorChar +
        ( SystemUtils.IS_OS_WINDOWS ? "mvn.bat" : "mvn" );
  }

  // mvn command life cycles and options
  private final static String MVN_COMMAND;
  private final static String MVN_CLEAN_LIFECYCLE = "clean";
  private final static String MVN_INSTALL_LIFECYCLE = "install";
  private final static String MVN_OFFLINE_OPTION = "-o";
  private final static String MVN_PROJECT_OPTION = "-pl";
  private final static String MVN_MODULE_SEPARATOR = ",:";

  /** logger */
  private static final Logger LOGGER = Logger.getLogger( BuildStrategy.class.getName() );

  public abstract Process buildProcess( Collection< ScmFile > files, File workingDir, CommandLine cmd )
      throws IOException;

  public static BuildStrategy resolveStrategy( Collection< ScmFile > files ) {
    String pom = File.separator + "pom.xml";
    for ( ScmFile file : files ) {
      if ( file.getPath().contains( pom ) ) return ALL;
    }
    return SELECTIVE;
  }
}
