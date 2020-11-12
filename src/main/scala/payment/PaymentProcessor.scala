package payment

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import payment.functions.PaymentParticipantProcessing
import transaction.{LoggingMessage, ParticipantData}


class PaymentProcessor extends FlinkStreamlet {

  private val formattedPaymentIn: AvroInlet[FormattedPayment] =
    AvroInlet[FormattedPayment]("payment-in")

  private val participantInitializerIn: AvroInlet[ParticipantData] =
    AvroInlet[ParticipantData]("participant-in")

  private val loggingMessageOut: AvroOutlet[LoggingMessage] =
    AvroOutlet[LoggingMessage]("logging-out")

  override def shape(): StreamletShape = StreamletShape.withInlets(formattedPaymentIn, participantInitializerIn)
    .withOutlets(loggingMessageOut)

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {
    override def buildExecutionGraph(): Unit = {
      val formattedPaymentStream: DataStream[FormattedPayment] = readStream(formattedPaymentIn)
      val participantStream: DataStream[ParticipantData] = readStream(participantInitializerIn)

      val connectedStreams = formattedPaymentStream.connect(participantStream)
        .keyBy(0,0).flatMap(new PaymentParticipantProcessing)
    }
  }
}
