package com.github.wisebrains.scm.build.stream;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * A stream gobbler {@link Runnable} thant collects input from a source stream and prints
 * it to a {@link PrintStream}.
 *
 * @author Marwen Trabesli
 * @since 0.0.1
 */
public class StreamGobbler implements Runnable {

  private final PrintStream destination;
  private final InputStream source;
  private final String prefix;

  public StreamGobbler( InputStream source, PrintStream destination, String prefix ) {
    this.destination = destination;
    this.source = source;
    this.prefix = StringUtils.isNotBlank( prefix ) ? prefix + " " : StringUtils.EMPTY;
  }

  @Override
  public void run() {
    Scanner scanner = new Scanner( source );
    while ( scanner.hasNextLine() ) {
      destination.println( prefix + scanner.nextLine() );
    }
  }
}
