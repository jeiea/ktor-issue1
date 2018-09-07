package github.jeiea.ktorreport

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.content.forEachPart
import io.ktor.http.takeFrom
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.experimental.runBlocking
import org.apache.http.HttpHost
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
  val server = embeddedServer(Netty, 8080, module = Application::testServer)
  server.start()

  runBlocking {
    val http = HttpClient(Apache.create {
      customizeClient {
//        setProxy(HttpHost("localhost", 8888))
      }
      socketTimeout = 0
      connectTimeout = 0
      connectionRequestTimeout = 0
    })

    for (cnt in 1..10000) {
      val form = formData {
        append("cnt_$cnt") {
          for (i in 0..4000) {
            writeByte(i.toByte())
          }
        }
      }
      http.submitFormWithBinaryData<Unit>(form) {
        url.takeFrom("http://localhost:8080/")
      }
    }
  }
  server.stop(1, 1, TimeUnit.SECONDS)
}

fun Application.testServer() {
  routing {
    post("/") { _ ->
      call.receiveMultipart().forEachPart {
        it.dispose()
      }
      call.respond("success")
    }
  }
}