package logging


import akka.stream.scaladsl.{Flow, RunnableGraph, Sink}
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import transaction.LoggingMessage

class PaymentLoggingEgress extends AkkaStreamlet {

  private val in = AvroInlet[LoggingMessage]("logger-in")

  override def shape(): StreamletShape = StreamletShape(in)

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {
    override def runnableGraph(): RunnableGraph[_] = {
      plainSource(in).via(logging).to(Sink.ignore)
    }
  }

  private def logging = Flow[LoggingMessage]
    .map { message =>
      message.logLevel match {
        case WARNING.level => log.warn(s"${message.message} with content ${message.rawContent}")
        case INFO.level => log.info(s"${message.message} with content ${message.rawContent}")
      }
    }
}

