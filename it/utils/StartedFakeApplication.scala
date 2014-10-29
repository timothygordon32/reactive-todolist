package utils

import play.api.Play
import play.api.test.{FakeApplication, PlaySpecification}

/**
 * Fire up a play application before running tests in the specification. Untidily, shutdown of the application will
 * be when the process terminates. Maybe something that 'counts in' and 'counts out' the running tests can calls
 * Play.stop() for clean shutdown per process.
 * <p/>
 * See <a href="https://github.com/ReactiveMongo/Play-ReactiveMongo/issues/7">this discussion</a>.
 */
trait StartedFakeApplication {
  self: PlaySpecification =>

  lazy implicit val app = new FakeApplication()

  step(Play.start(app))
}
