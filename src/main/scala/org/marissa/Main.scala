package org.marissa.client

import org.marissa.core.Marissa

/**
  * Created by mjw on 06/08/2016.
  */
object Main extends App {

  // connection details

  val details = new ConnectionDetails(
    user = "marissabot",
    pass = "pass",
    nick = "nick",
    rooms = List("room")
  )

  // create the bot

  val bot = Marissa(details)

  // attach some example handlers

  bot.handler((tx, rx) => println("Saw a message: " + rx.take().body) )
  bot.handler((tx, rx) => println("Saw a message: " + rx.take().body) )

  // start (this will block)

  bot.start()

}
