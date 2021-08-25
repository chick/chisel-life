// SPDX-License-Identifier: Apache-2.0

package life

import chiseltest.ChiselScalatestTester
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LifeTest extends AnyFreeSpec with ChiselScalatestTester with Matchers {
  "should simulate life game" in {
    test(new AddressableLifeGrid(20, 20)) { c =>
      new ExampleTests(c)
    }
  }
}
