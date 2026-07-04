package nodomain.seven.dip.api

import io.quarkus.security.UnauthorizedException
import jakarta.ws.rs.BadRequestException
import nodomain.seven.dip.utils.filePath
import nodomain.seven.dip.utils.setupFiles
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path

object UserDao {
    val userDataPath: Path = filePath.resolve("users")

    init {
        if (!Files.exists(filePath)) setupFiles(filePath)
        if (!Files.exists(userDataPath)) {
            Files.createDirectory(userDataPath)
        }
    }

    fun find(user: User): Boolean = Files.exists(userDataPath.resolve("${user.name}.ser"))

    fun signUp(user: User) {
        if (find(user)) {
            throw BadRequestException("User name already taken")
        }
        Files.createFile(userDataPath.resolve("${user.name}.ser"))
        saveData(user)
    }

    fun saveData(user: User) {
        ObjectOutputStream(BufferedOutputStream(FileOutputStream(userDataPath.resolve("${user.name}.ser").toFile())))
            .use { it.writeObject(user) }
    }

    fun login(logIn: User): User {
        val  userFile = userDataPath.resolve("${logIn.name}.ser")
        if (!Files.exists(userFile)) {
            throw UnauthorizedException("Not authenticated")
        }
        val user = ObjectInputStream(BufferedInputStream(FileInputStream(userFile.toFile()))).use {
            it.readObject() as User
        }
        if (user.password != logIn.password) {
            throw UnauthorizedException("Not authenticated")
        }
        return user
    }

    fun getUser(name: String): User {
       return ObjectInputStream(BufferedInputStream(FileInputStream(userDataPath.resolve("$name.ser").toFile()))).use {
            it.readObject() as User
       }
    }
}