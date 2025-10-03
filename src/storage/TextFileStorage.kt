package storage

import java.nio.file.*
import kotlin.io.path.*

class TextFileStorage<K, D: Any>(
    baseFolder: String,
    private val serializer: Serializer<D>,
) : Storage<K, D> {

    private val basePath = Path(baseFolder)

    init {
        with(basePath) {
            if (exists())
                check(isDirectory()) { "$name is not a directory" }
            else createDirectory()
        }
    }

    private fun <T> withPath(key: K, fx: Path.() -> T) =
        (basePath / "$key.txt").fx()

    override fun create(key: K, data: D) = withPath(key) {
        check(!exists()) { "$name already exists" }
        writeText(serializer.serialize(data))
    }

    override fun read(key: K): D? = withPath(key) {
        try { serializer.deserialize(readText()) }
        catch (e: NoSuchFileException) { null }
    }

    override fun update(key: K, data: D) = withPath(key) {
        check(exists()) { "$name does not exist" }
        writeText(serializer.serialize(data))
    }

    override fun delete(key: K) = withPath(key) {
        check(deleteIfExists()) { "$name does not exist" }
    }
}
