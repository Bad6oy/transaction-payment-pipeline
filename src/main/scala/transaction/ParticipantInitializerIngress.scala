package transaction

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cloudflow.akkastream.util.scaladsl.HttpServerLogic
import cloudflow.akkastream.{AkkaServerStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroOutlet
import transaction.ParticipantJsonProtocol.ParticipantInitializerData

class ParticipantInitializerIngress extends AkkaServerStreamlet{

  private val participantOutlet = AvroOutlet[ParticipantData]("participant-out", _.id)

  override protected def createLogic(): AkkaStreamletLogic = HttpServerLogic.default(this, participantOutlet)

  override def shape(): StreamletShape = StreamletShape.withOutlets(participantOutlet)
}
