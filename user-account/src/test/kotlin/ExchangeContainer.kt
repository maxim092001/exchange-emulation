import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import java.nio.file.Path

class ExchangeContainer(private val port: Int) {
    private val dockerfilePath = Path.of("..").resolve("Dockerfile")
    private val image = ImageFromDockerfile()
        .withDockerfile(dockerfilePath)

    private val containerImpl = GenericContainer(image)
        .withEnv("EXCHANGE_PORT", port.toString())
        .apply {
            setPortBindings(listOf("$port:$port"))
        }

    fun start() = containerImpl.start()

    fun stop() = containerImpl.stop()
}