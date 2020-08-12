package com.example.dsls

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class DslsApplication

fun main(args: Array<String>) {
  runApplication<DslsApplication>(*args) {
    addInitializers(beans {
      bean {
				router {
					GET("/hello") {
						ServerResponse.ok().bodyValue(mapOf("greeting" to "Hello, world!"))
					}
				}
      }
      bean {
         ref <RouteLocatorBuilder> ().routes {
           route {
             path("/proxy")
             filters {
               setPath("/guides")
             }
             uri("https://spring.io/")
           }
         }
      }
    })
  }
}

