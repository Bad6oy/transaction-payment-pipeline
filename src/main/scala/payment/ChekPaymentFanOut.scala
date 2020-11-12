package payment

import Utility.Messages.{formattedPaymentMessage, invalidPaymentDataMessage}
import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{RoundRobinPartitioner, StreamletShape}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector
import transaction.LoggingMessage


class ChekPaymentFanOut extends FlinkStreamlet {

  private val formattedPaymentEgress: AvroOutlet[FormattedPayment] =
    AvroOutlet[FormattedPayment]("formattedPaymentOut").withPartitioner(RoundRobinPartitioner)

  private val invalidPaymentEgress: AvroOutlet[LoggingMessage] =
    AvroOutlet[LoggingMessage]("invalidPaymentOut").withPartitioner(RoundRobinPartitioner)

  private val rawPaymentIngress: AvroInlet[RawFileData] = AvroInlet[RawFileData]("rawPaymentIn")

  private val formattedMessageTag = OutputTag[FormattedPayment]("formatted-output")
  private val invalidMessageTag = OutputTag[LoggingMessage]("invalid-output")

  private val regexFilter = """<(a-Z)> -> <(a-Z)>: <(\d>)""".r

  override def shape(): StreamletShape = {
    StreamletShape.withInlets(rawPaymentIngress).withOutlets(formattedPaymentEgress, invalidPaymentEgress)
  }

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {
    override def buildExecutionGraph(): Unit = {

      val stream = readStream(rawPaymentIngress).process(new ProcessFunction[RawFileData, FormattedPayment] {
        override def processElement(value: RawFileData, ctx: ProcessFunction[RawFileData, FormattedPayment]#Context,
                                    out: Collector[FormattedPayment]): Unit = {
          value.content match {
            case regexFilter(from, to, amount) => ctx.output(formattedMessageTag, formattedPaymentMessage(from, to, amount))
            case _ => ctx.output(invalidMessageTag, invalidPaymentDataMessage(value.content))
          }
        }
      })

      writeStream(formattedPaymentEgress, stream.getSideOutput(formattedMessageTag))
      writeStream(invalidPaymentEgress, stream.getSideOutput(invalidMessageTag))
    }
  }
}
