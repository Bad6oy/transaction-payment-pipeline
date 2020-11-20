package ru.neoflex.transaction

import cloudflow.akkastream.util.scaladsl.HttpServerLogic
import cloudflow.akkastream.{AkkaServerStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroOutlet
import ru.neoflex.transaction.ParticipantJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class ParticipantInitializerIngress extends AkkaServerStreamlet {

  @transient private val participantOutlet = AvroOutlet[ParticipantData]("out", _.id)

  @transient override def shape(): StreamletShape = StreamletShape.withOutlets(participantOutlet)

  override protected def createLogic(): AkkaStreamletLogic = HttpServerLogic.default(this, participantOutlet)
}
