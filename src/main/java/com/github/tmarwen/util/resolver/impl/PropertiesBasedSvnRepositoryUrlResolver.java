package com.github.tmarwen.util.resolver.impl;

import com.github.tmarwen.util.resolver.SvnRepositoryUrlResolver;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Implementation of the {@link SvnRepositoryUrlResolver} that resolve the svn repository
 * url based on the working copy svn information.
 *
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class PropertiesBasedSvnRepositoryUrlResolver implements SvnRepositoryUrlResolver {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger( PropertiesBasedSvnRepositoryUrlResolver.class.getName() );

  /** command line default timeout */
  private static final int TIMEOUT = 3;

  /** svn command to retrieve properties information */
  private static final String SVN_INFO_CMD = "svn info";

  /** repository prefix, used to bypass repository checking by the wrapped scm manager */
  private static final String SVN_SCM_PREFIX = "scm:svn:";

  /** delimiter for svn information properties key and value */
  private static final char PROPERTY_KEY_DELIMITER = ':';

  /** key of the svn url property */
  private static final String URL_PROPERTY_KEY = "URL";

  @Override
  public String resolveRepositoryUrl( File workingRepoDirectory ) {
    String repositoryUrl = null;
    try {
      // 'svn info' to guess all information
      CommandLineUtils.StringStreamConsumer resultConsumer = new CommandLineUtils.StringStreamConsumer();
      Commandline svnInfoCmd = new Commandline( SVN_INFO_CMD );
      svnInfoCmd.setWorkingDirectory( workingRepoDirectory );
      CommandLineUtils.executeCommandLine( svnInfoCmd, resultConsumer, new DefaultConsumer(), TIMEOUT );
      String[] repositoryProperties = resultConsumer.getOutput().split( SystemUtils.LINE_SEPARATOR );
      for (String property : repositoryProperties) {
        int delimiterIndex = property.indexOf( PROPERTY_KEY_DELIMITER );
        if ( delimiterIndex != -1 ) {
          String metadataPropKey = property.substring( 0, delimiterIndex );
          if ( URL_PROPERTY_KEY.equals( metadataPropKey ) ) {
            repositoryUrl = SVN_SCM_PREFIX + property.substring( delimiterIndex + 1, property.length() ).trim();
            LOGGER.info( "SVN repository url resolved: '" + repositoryUrl + "'" );
            break;
          }
        }
      }
    } catch ( CommandLineException e ) {
      LOGGER.warning( e.getMessage() );
    }
    return repositoryUrl;
  }
}
