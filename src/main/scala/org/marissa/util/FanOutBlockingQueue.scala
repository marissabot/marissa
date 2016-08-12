package org.marissa.util

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FanOutBlockingQueue[T](val incoming: BlockingQueue[T]) {

  var fanOuts: List[BlockingQueue[T]] = List()

  Future{

    while(true) {
      val in: T = incoming.take()
      fanOuts.foreach(q => q.add(in))
    }

  }

  def hook(): BlockingQueue[T] = {

    val q = new LinkedBlockingQueue[T](10)

    fanOuts = fanOuts :+ q

    q


  }

}
