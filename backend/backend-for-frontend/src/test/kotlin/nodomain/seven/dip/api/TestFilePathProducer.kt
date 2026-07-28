package nodomain.seven.dip.api

import io.quarkus.test.Mock
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nodomain.seven.dip.file.BaseFilePathProvider
import java.nio.file.Files
import java.nio.file.Path


@Mock
@ApplicationScoped
class TestFilePathProducer: BaseFilePathProvider() {
    init {
        val testFilePath = filePath()
        if (Files.exists(testFilePath)) {
            Files.walk(testFilePath).use {
                it.sorted(reverseOrder()).forEach(Files::delete)
            }
        }
    }

    @Produces
    override fun filePath(): Path = super.filePath().resolve("test")
}