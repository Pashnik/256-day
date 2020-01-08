import cats.effect.Sync
import io.circe.Encoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

package object endpoints {
  implicit def jsonEncoder[F[_]: Sync, A: Encoder]: EntityEncoder[F, A] =
    jsonEncoderOf[F, A]
}
