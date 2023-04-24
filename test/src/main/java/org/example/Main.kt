package org.example

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val output = BlockingByteArrayOutputStream()

        val input = output

        println("Before")

        //create two threads and join them

        val first = Thread {
            println("Starting read")
            println(input.read())
            println(input.read())
            println("Finished read")
        }

        val second = Thread {
            println("Starting sleep")
            Thread.sleep(1000)
            println("Finished sleep")
            println("Starting write")
            for (i in 0..5) {
                output.write(byteArrayOf(i.toByte()))
                Thread.sleep(10)
            }
            println("Finished write")
        }

        first.start()
        second.start()

        //wait for both threads to finish
        first.join()
        second.join()


        println("After")

    }
}
