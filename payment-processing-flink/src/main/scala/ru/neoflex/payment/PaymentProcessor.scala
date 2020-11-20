package ru.neoflex.payment

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import ru.neoflex.payment.functions.PaymentParticipantProcessing
import ru.neoflex.transaction.{LoggingMessage , ParticipantData}


class PaymentProcessor extends FlinkStreamlet {

  @transient private val formattedPaymentIn: AvroInlet[FormattedPayment] =
    AvroInlet[FormattedPayment]("payment-in")

  @transient private val participantInitializerIn: AvroInlet[ParticipantData] =
    AvroInlet[ParticipantData]("participant-in")

  @transient private val loggingMessageOut: AvroOutlet[LoggingMessage] =
    AvroOutlet[LoggingMessage]("logging-out")

  @transient override def shape(): StreamletShape = StreamletShape.withInlets(formattedPaymentIn, participantInitializerIn)
    .withOutlets(loggingMessageOut)

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {
    override def buildExecutionGraph(): Unit = {
      val formattedPaymentStream: DataStream[FormattedPayment] = readStream(formattedPaymentIn)
      val participantStream: DataStream[ParticipantData] = readStream(participantInitializerIn)

      formattedPaymentStream.connect(participantStream)
        .keyBy(0, 0).flatMap(new PaymentParticipantProcessing)
    }
  }
}
