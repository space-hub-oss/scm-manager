package com.github.tmarwen.util.resolver.impl;

import com.github.tmarwen.util.resolver.MavenProjectDirectoryResolver;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Default implementation of {@link MavenProjectDirectoryResolver} that resolves the
 * maven root project directory based on a direcotry traversal.
 *
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class DefaultMavenProjectDirectoryResolver implements MavenProjectDirectoryResolver {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger( DefaultMavenProjectDirectoryResolver.class.getName() );

  /** maven pom file name template */
  private static final String POM_FILE = "pom.xml";

  @Override
  public final File resolve(final Path current, final String name, final boolean exists) {
    File asFile = current.toFile();
    Preconditions.checkArgument( !exists || asFile.exists(), "The working directory '%s' doesn't exist.",
        asFile.getAbsolutePath() );

    final File result;
    if ( StringUtils.isNotBlank(name) ) { // search for the specified directory
      if ( name.equals( asFile.getName() ) ) { // just in the right location
        result = asFile;
      } else if ( Arrays.asList( asFile.list() ).contains(name) ) { // just in the root of the svn repo
        result = new File( asFile.getAbsolutePath() + File.separator + name);
      } else { // go all the way up
        result = this.resolve(current.getParent(), name, false);
      }
    } else {
      if (this.isMavenModule( current.getParent() ) ) { // parent is a module, go a step up
        result = this.resolve(current.getParent(), name, false);
      } else { // we are at the top most directory with a 'pom.xml' file
        result = current.toAbsolutePath().toFile();
      }
    }
    return result;
  }

  private boolean isMavenModule( final Path directory ) {
    boolean result = false;
    try ( DirectoryStream<Path> paths = Files.newDirectoryStream( directory ) ) {
      for ( final Path path : paths ) {
        if (DefaultMavenProjectDirectoryResolver.POM_FILE.equals(path.toFile().getName()) ) {
          result = true; break;
        }
      }
    } catch ( final IOException e ) {
      DefaultMavenProjectDirectoryResolver.LOGGER.warning(
          String.format("An error occurred while resolving a maven module [ %s ]:%n%s", directory, e.getMessage())
      );
    }
    return result;
  }

}
