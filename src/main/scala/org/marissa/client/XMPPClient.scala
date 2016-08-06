package org.marissa.client

import rocks.xmpp.core.Jid
import rocks.xmpp.core.XmppException
import rocks.xmpp.core.session._
import rocks.xmpp.core.stanza.MessageEvent
import rocks.xmpp.core.stanza.MessageListener
import rocks.xmpp.core.stanza.model.AbstractPresence
import rocks.xmpp.core.stanza.model.client.Presence
import rocks.xmpp.extensions.muc.ChatRoom
import rocks.xmpp.extensions.muc.ChatService
import rocks.xmpp.extensions.muc.MultiUserChatManager
import rocks.xmpp.extensions.muc.model.History
import java.util.concurrent.BlockingQueue

import scala.util.Try

object XMPPClient {

  def apply(
     connectionDetails: ConnectionDetails,
     rxChannel: BlockingQueue[ChatMessage],
     txChannel: BlockingQueue[ChatMessage]
   ): XMPPClient = {
    new XMPPClient(connectionDetails, rxChannel, txChannel)
  }

}

class XMPPClient(val connectionDetails: ConnectionDetails, val rxChannel: BlockingQueue[ChatMessage], val txChannel: BlockingQueue[ChatMessage]) {

  var joinedRooms: Map[String, ChatRoom] = Map()
  val configuration = XmppSessionConfiguration.builder().build()
  val xmppSession = new XmppSession("chat.hipchat.com", configuration)

  object listener extends MessageListener {
    override def handleMessage(e: MessageEvent): Unit = {

      println("handling message")

      val sender = e.getMessage.getFrom.getResource
      val isMe = sender == connectionDetails.nick
      val msg = ChatMessage(e.getMessage.getFrom.getLocal, e.getMessage.getBody)

      if( ! isMe ) {
        rxChannel.add(msg)
      }

    }
  }

  /**
    * Attempts to gracefully disconnect from the XMPP Session
    */
  def die(reason: Throwable) {

    println("XMPPClient has died", reason)

    Try {

      if (xmppSession.isConnected) {
        xmppSession.send(new Presence(AbstractPresence.Type.UNAVAILABLE))
      }

      xmppSession.close()

    } recover {
      case e: Exception => throw new IllegalStateException("failed to die cleanly (with presence)")
    }

  }

  /**
    * Given a list of rooms, joins them all.
    * We leave any rooms we're currently in before doing this.
    */
  def joinRooms(joinRooms: List[String]) {

    val m = xmppSession.getManager(classOf[MultiUserChatManager])

    val chatService = m.createChatService(Jid.valueOf("conf.hipchat.com"))

    // leave any rooms we're already in
    println("leaving rooms")

    joinedRooms.foreach{ case (key, value) => leaveRoom(value) }

    // ok now join the new rooms

    println("joining rooms")
    joinRooms.foreach(room => joinRoom(chatService, room))

  }

  /**
    * Leaves a single room gracefully
    */
  def joinRoom(chatService: ChatService, room: String) {

    val cr = chatService.createRoom(room)

    Try {
      cr.addInboundMessageListener(listener)
      cr.enter(connectionDetails.nick, History.forMaxMessages(0))
      joinedRooms = joinedRooms + (room->cr)
    } recover {
      case e: Throwable => println("Failed to join room", e)
    }

    joinedRooms = joinedRooms + (room->cr)

  }

  /**
    * Joins a single room
    */
  def leaveRoom(room: ChatRoom) {
    Try {
      room.removeInboundMessageListener(this.listener)
      room.exit()
    } recover {
      case e: Exception => println("Failed to leave room", e)
    }
  }

  /**
    * Connect to chat and start handling messages
    */
  def start() {

    // connect to XMPP

    xmppSession.connect()
    xmppSession.login(connectionDetails.user, connectionDetails.pass)
    xmppSession.send(new Presence())

    println("connected")

    // join the rooms

    joinRooms(connectionDetails.rooms)

    // reconnect listener

    xmppSession.addSessionStatusListener(new SessionStatusListener {
      override def sessionStatusChanged(e: SessionStatusEvent): Unit =

        if (e.getStatus == XmppSession.Status.AUTHENTICATED) {

          Try {
            joinRooms(connectionDetails.rooms)
          } recover {
            case ex: XmppException => println(ex)
          }

        } else {
          println("Received unhandled session status: " + e.getStatus)
        }

    })

    selectMessageLoop()

  }

  /**
    * Runs a thread that waits for messages on the txChannel and sends them to the remote xmpp service.
    */
  def selectMessageLoop() {

    println("Entering blocking loop")

    while(true) {

      val chatMessage: ChatMessage = txChannel.take

      joinedRooms.get(chatMessage.from) match {
        case Some(room) => room.sendMessage(chatMessage.body)
        case None => println("Chatroom isn't joined " + chatMessage.from)
      }

    }

  }

}
