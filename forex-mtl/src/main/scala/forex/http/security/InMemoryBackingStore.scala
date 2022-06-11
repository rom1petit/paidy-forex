package forex.http.security

import cats.data.OptionT
import cats.effect.{Ref, Sync}
import cats.implicits.toFunctorOps
import tsec.authentication.BackingStore

object InMemoryBackingStore {

  def apply[F[_], I, V](ref: Ref[F, Map[I, V]], getId: V => I)(implicit F: Sync[F]): BackingStore[F, I, V] =
    new BackingStore[F, I, V] {

      def put(elem: V): F[V] =
        ref.modify(store => (store + (getId(elem) -> elem), elem))

      def get(id: I): OptionT[F, V] =
        OptionT(ref.get.map(_.get(id)))

      def update(elem: V): F[V] =
        put(elem)

      def delete(id: I): F[Unit] =
        ref.modify(store => (store - id, ()))
    }
}
