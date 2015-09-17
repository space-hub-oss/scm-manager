package com.github.tmarwen.util.resolver;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public interface MavenProjectDirectoryResolver {

    File resolve(Path current, String name, boolean exists);

}