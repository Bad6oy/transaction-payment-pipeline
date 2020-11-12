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

  private def format(message: LoggingMessage) = {
    s"${message.message}. Additional information was ${message.content}"
  }

  private def logging = Flow[LoggingMessage]
    .map { message =>
      message.logLevel match {
        case "WARN" => log.warn(format(message))
        case "INFO" => log.info(format(message))
      }
    }
}

