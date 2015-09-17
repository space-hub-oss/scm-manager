package com.github.wisebrains.util.resolver;

import java.io.File;

/**
 * Strategy based svn repository url resolver.
 *
 * @author Marwen Trabelsi (marwen.trabelsi.insat@gmail.com)
 * @since 0.0.1
 */
public interface SvnRepositoryUrlResolver {
    /**
     * Resolve the SVN repository url based on a custom strategy.
     *
     * @param file The svn repository directory
     * @return The svn repository url string representation
     */
    String resolveRepositoryUrl(File file);
}