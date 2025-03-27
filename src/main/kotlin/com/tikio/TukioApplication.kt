package com.tikio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class TukioApplication

fun main(args: Array<String>) {
    runApplication<TukioApplication>(*args)
}
