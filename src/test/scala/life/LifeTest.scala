// SPDX-License-Identifier: Apache-2.0

package life

import chisel3.iotesters.Driver
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LifeTest extends AnyFreeSpec with Matchers {
  "should simulate life game" in {
    Driver.execute(Array("-tiv"), () => new AddressableLifeGrid(20, 20)) { c =>
      new AddressableLifeGridTests(c)
    }
  }
}
