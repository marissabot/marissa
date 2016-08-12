package org.marissa.core

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import org.marissa.client.{ChatMessage, ConnectionDetails, XMPPClient}
import org.marissa.util.FanOutBlockingQueue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Marissa(val details: ConnectionDetails) {

  val rxChannel : BlockingQueue[ChatMessage] = new LinkedBlockingQueue[ChatMessage]()
  val txChannel : BlockingQueue[ChatMessage] = new LinkedBlockingQueue[ChatMessage]()
  val rxcChannel: FanOutBlockingQueue[ChatMessage] = new FanOutBlockingQueue[ChatMessage](rxChannel)

  val client = XMPPClient(
    details, rxChannel, txChannel
  )

  def start(): Unit = {
    client.start()
  }

  def handler(f: (BlockingQueue[ChatMessage], BlockingQueue[ChatMessage]) => Unit) = {
    Future { f(txChannel, rxcChannel.hook()) }
  }

}

object Marissa {
  def apply(details: ConnectionDetails): Marissa = {
    new Marissa(details)
  }
}