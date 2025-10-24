package storage

/**
 * Interface genérica de serialização e desserialização de objetos.
 */
interface Serializer<T> {
    fun serialize(obj: T): String
    fun deserialize(data: String): T
}