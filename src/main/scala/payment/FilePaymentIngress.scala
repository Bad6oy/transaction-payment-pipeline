package payment


import java.nio.file.FileSystems


import akka.stream.alpakka.file.javadsl.Directory
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import org.apache.kafka.clients.producer.RoundRobinPartitioner

class FilePaymentIngress extends AkkaStreamlet {

  val pathToDirectory = ""

  val incomingTransaction = AvroInlet[RawFileData]("in")
  val outputTransactionData = AvroInlet[RawFileData]("out").withPartitioner(RoundRobinPartitioner)


  val fileSystem = FileSystems.getDefault

  //Directory.ls(fileSystem.getPath(pathToDirectory)).flatMapConcat(Directory.ls)
  override def shape(): StreamletShape = StreamletShape.withOutlets(outputTransactionData)
  override protected def createLogic(): AkkaStreamletLogic = new AkkaStreamletLogic() {
    override def run(): Unit = {

    }
  }
}
