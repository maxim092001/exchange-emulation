package client

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class BaseHttpClient(private val baseUrl: String) {
    private val clientImpl = HttpClient.newHttpClient()

    protected fun req(path: String, params: Map<String, String>): String {
        val url = "$baseUrl$path?${params.toList().joinToString { "${it.first}=${it.second}" }}"
        return clientImpl.send(
            HttpRequest.newBuilder(URI(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        ).body()
    }
}