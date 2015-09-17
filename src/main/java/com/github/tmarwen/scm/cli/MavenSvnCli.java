package com.github.tmarwen.scm.cli;

import com.github.tmarwen.scm.ScmRepositoryManager;
import com.github.tmarwen.scm.binding.MavenSvnModule;
import com.github.tmarwen.scm.binding.annotation.SvnManager;
import com.github.tmarwen.scm.build.BuildException;
import com.github.tmarwen.scm.build.BuildHandler;
import com.github.tmarwen.scm.build.BuildStrategy;
import com.github.tmarwen.scm.build.recovery.BuildRecovery;
import com.github.tmarwen.scm.cli.option.MavenSvnCliOptionHandler;
import com.github.tmarwen.util.resolver.MavenProjectDirectoryResolver;
import com.github.tmarwen.util.resolver.SvnRepositoryUrlResolver;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.scm.ScmFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main entry to the application providing facility to update an svn repository
 * and perform a maven build depending on the files updated.
 *
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class MavenSvnCli {

  private static final String URL_SEPARATOR = "/";

  private final Logger logger;
  private final ScmRepositoryManager scmManager;
  private final SvnRepositoryUrlResolver repositoryUrlResolver;
  private final MavenProjectDirectoryResolver projectDirectoryResolver;
  private final BuildRecovery buildRecovery;
  private final Config configuration;

  @Inject
  public MavenSvnCli( @SvnManager ScmRepositoryManager scmManager, SvnRepositoryUrlResolver urlResolver,
                      MavenProjectDirectoryResolver directoryResolver, BuildRecovery buildRecovery, Config config,
                      Logger logger ) {
    this.scmManager = scmManager;
    this.repositoryUrlResolver = urlResolver;
    this.projectDirectoryResolver = directoryResolver;
    this.buildRecovery = buildRecovery;
    this.configuration = config;
    this.logger = logger;
  }

  public static void main( String[] args ) {

    CommandLine commandLine = MavenSvnCliOptionHandler.parseApplicationArguments( args );
    if ( !( commandLine.hasOption( "up" ) ^ commandLine.hasOption( "r" ) ) ) { // TODO: more options are to come
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( "scm-manager", MavenSvnCliOptionHandler.defaultOptions() );
      System.exit( 0 );
    }

    // bootstrap injector
    final Injector injector = Guice.createInjector( new MavenSvnModule() );
    final MavenSvnCli cli = injector.getInstance(MavenSvnCli.class);
    if ( commandLine.hasOption( "r" ) ) {
      cli.getBuildRecovery().recover(
          cli.getProjectDirectoryResolver().resolve(
              Paths.get("").toAbsolutePath(),
              cli.getConfiguration().getString("application.root.directory"),
              true),
          commandLine );
    } else if ( commandLine.hasOption( "up" ) ) {
      cli.updateAndBuild(commandLine);
    }
  }

  private void updateAndBuild( CommandLine commandLine ) {
    File rootDirectory = getProjectDirectoryResolver().resolve(
        Paths.get("").toAbsolutePath(),
        getConfiguration().getString("application.root.directory"),
        true);
    String svnRepoUrl = getRepositoryUrlResolver().resolveRepositoryUrl( rootDirectory );
    // project root directory and scm url may not match each other, we should align them
    String adjustRepositoryUrl = adjustRepositoryUrl( svnRepoUrl, rootDirectory.getAbsolutePath() );
    try {
      List< ScmFile > updatedFiles = getScmManager().update( adjustRepositoryUrl, rootDirectory );
      build( updatedFiles, rootDirectory, commandLine );
    } catch ( Exception e ) {
      logger.warning( "Cannot update the svn repository" + SystemUtils.LINE_SEPARATOR + e.getMessage() );
    }
  }

  private void build( List< ScmFile > files, File workingDirectory, CommandLine commandLine ) {
    if ( CollectionUtils.isNotEmpty( files ) ) {
      logger.info( "Performing a build under the '" + workingDirectory.getAbsolutePath() + "' directory:" );
      BuildStrategy strategy = BuildStrategy.resolveStrategy( files );
      BuildHandler buildHandler = new BuildHandler( strategy );
      try {
        buildHandler.build( files, workingDirectory, commandLine );
      } catch ( BuildException e ) {
        buildRecovery.backup( files, workingDirectory );
        logger.warning( "An error occurred while triggering the maven build:" +
            SystemUtils.LINE_SEPARATOR + e.getMessage() );
      }
    } else {
      logger.info( "The working copy is already up to date." );
    }
  }

  private String adjustRepositoryUrl( String repositoryUrl, String mavenProjectPath ) {
    String rootFilePath = mavenProjectPath.substring( mavenProjectPath.lastIndexOf( File.separator ) + 1 );
    if ( rootFilePath.equals( repositoryUrl.substring( repositoryUrl.lastIndexOf( URL_SEPARATOR ) + 1 ) ) ) { // aligned
      return repositoryUrl;
    } else {
      int projectRootIndex = repositoryUrl.indexOf( rootFilePath );
      if ( projectRootIndex == -1 ) { // project root not here, concatenate and return
        return repositoryUrl + URL_SEPARATOR + rootFilePath;
      } else { // drop useless url path parts
        return repositoryUrl.substring( 0, projectRootIndex + rootFilePath.length() );
      }
    }
  }

  public ScmRepositoryManager getScmManager() {
    return scmManager;
  }

  public SvnRepositoryUrlResolver getRepositoryUrlResolver() {
    return repositoryUrlResolver;
  }

  public MavenProjectDirectoryResolver getProjectDirectoryResolver() {
    return projectDirectoryResolver;
  }

  public BuildRecovery getBuildRecovery() {
    return buildRecovery;
  }

  public Config getConfiguration() {
    return configuration;
  }
}
