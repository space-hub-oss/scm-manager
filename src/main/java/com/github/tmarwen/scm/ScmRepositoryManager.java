package com.github.tmarwen.scm;

import org.apache.maven.scm.ScmFile;

import java.io.File;
import java.util.List;

/**
 * Basic template of a source code version system manager that performs actions against
 * a repository, such as update, etc.
 *
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public interface ScmRepositoryManager {

  List< ScmFile > update( String scmUrl, File workingDirectory ) throws Exception;

}
