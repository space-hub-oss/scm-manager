package com.github.wisebrains.scm.cli.option;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.logging.Logger;

/**
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class MavenSvnCliOptionHandler {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger( MavenSvnCliOptionHandler.class.getName() );

  private MavenSvnCliOptionHandler() {
  }

  public static CommandLine parseApplicationArguments( String[] args ) {
    CommandLine commandLine = null;
    CommandLineParser commandLineParser = new BasicParser();
    try {
      commandLine = commandLineParser.parse( defaultOptions(), args );
    } catch ( ParseException e ) {
      LOGGER.warning( "Cannot parse the application arguments" );
    }
    return commandLine;
  }

  public static Options defaultOptions() {
    return new Options()
        .addOption( new Option( "o", "online", false, "perform build in an online mode - default is offline" ) )
        .addOption( new Option( "up", "update", false, "update the svn repository" ) )
        .addOption( new Option( "r", "recover", false, "recovers a previousely failed build" ) );
  }

}
