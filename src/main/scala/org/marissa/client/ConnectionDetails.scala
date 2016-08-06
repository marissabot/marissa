package org.marissa.client

case class ConnectionDetails(
  user : String,
  pass : String,
  nick : String,
  rooms : List[String]
)
