package org.marissa.core

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import org.marissa.client.{ChatMessage, ConnectionDetails, XMPPClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Marissa(val details: ConnectionDetails) {

  val rxChannel: BlockingQueue[ChatMessage] = new LinkedBlockingQueue[ChatMessage]()
  val txChannel: BlockingQueue[ChatMessage] = new LinkedBlockingQueue[ChatMessage]()

  val client = XMPPClient(
    details, rxChannel, txChannel
  )

  def start(): Unit = {
    client.start()
  }

  def handler(f: (BlockingQueue[ChatMessage], BlockingQueue[ChatMessage]) => Unit) = {
    Future { f(txChannel, rxChannel) }
  }

}

object Marissa {
  def apply(details: ConnectionDetails): Marissa = {
    new Marissa(details)
  }
}