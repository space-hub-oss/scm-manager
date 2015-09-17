package com.github.wisebrains.scm.svn;

import com.github.wisebrains.scm.ScmRepositoryManager;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of {@link ScmRepositoryManager} that handles common SCM actions for an
 * svn repository.
 *
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class SvnScmManager implements ScmRepositoryManager {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger( SvnScmManager.class.getName() );

  /** the wrapped scm manager */
  private final ScmManager scmManager;

  public SvnScmManager() {
    this.scmManager = new BasicScmManager();
    this.scmManager.setScmProvider( "svn", new SvnExeScmProvider() );
  }

  public List< ScmFile > update( String scmUrl, File workingDirectory ) throws Exception {
    UpdateScmResult result = getScmManager().update( getScmRepository( scmUrl ), new ScmFileSet( workingDirectory ),
        false );
    LOGGER.info( "Repository updated successfully." );
    return result.getUpdatedFiles();
  }

  public ScmRepository getScmRepository( String scmUrl ) throws Exception {
    try {
      return getScmManager().makeScmRepository( scmUrl );
    } catch ( NoSuchScmProviderException ex ) {
      throw new Exception( "Could not find a provider.", ex );
    } catch ( ScmRepositoryException ex ) {
      throw new Exception( "Error while connecting to the repository", ex );
    }
  }

  public ScmManager getScmManager() {
    return scmManager;
  }
}
