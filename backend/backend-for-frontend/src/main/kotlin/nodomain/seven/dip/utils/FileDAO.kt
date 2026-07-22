package nodomain.seven.dip.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path

abstract class FileDAO<S, T> {
    abstract fun getPath(identifier: S): Path

    @Suppress("UNCHECKED_CAST")
    fun load(identifier: S): T {
        val loadPath = getPath(identifier)
        return ObjectInputStream(BufferedInputStream(FileInputStream(loadPath.toFile()))).use {
            it.readObject() as T
        }
    }

    fun createIfNotExists(identifier: S) {
        val creationPath = getPath(identifier)
        if (!Files.exists(creationPath.parent))
            Files.createDirectories(creationPath.parent)
        if (!Files.exists(creationPath)) {
            Files.createFile(creationPath)
            onCreation(identifier, creationPath)
        }
    }

    open fun onCreation(identifier: S, creationPath: Path) {}

    fun save(identifier: S, toBeSaved: T) {
        val savePath = getPath(identifier)
        ObjectOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    savePath.toFile()
                )
            )
        ).use {
            it.writeObject(toBeSaved)
        }
    }
}