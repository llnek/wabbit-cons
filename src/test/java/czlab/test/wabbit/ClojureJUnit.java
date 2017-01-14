/**
 * Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */

package czlab.test.xlib;

import mikera.cljunit.ClojureTest;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kenneth Leung
 */
public class ClojureJUnit extends ClojureTest {
  @Override
  public List<String> namespaces() {
    return Arrays.asList(new String[] {
      "czlab.test.xlib.coreutils",
      "czlab.test.xlib.dateutils",
      "czlab.test.xlib.misc",
      "czlab.test.xlib.ioutils",
      "czlab.test.xlib.metautils",
      "czlab.test.xlib.procutils",
      "czlab.test.xlib.strutils",
      "czlab.test.xlib.win32ini"
    });
  }
}


