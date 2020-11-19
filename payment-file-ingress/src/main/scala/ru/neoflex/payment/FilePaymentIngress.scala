package ru.neoflex.payment

import java.nio.file.{FileSystems, Path}

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl.{FileIO, Framing, RunnableGraph, Source}
import akka.util.ByteString
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.{RoundRobinPartitioner, StreamletShape}
import cloudflow.streamlets.avro.AvroOutlet
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future


class FilePaymentIngress extends AkkaStreamlet {

  private val delimiter: String = "\n"
  private val maxFrameLength: Int = 1024
  private val path = "neoflex.file-ingress.volume-mounts.source-data"
  private val outTransactionData: AvroOutlet[RawFileData] =
    AvroOutlet[RawFileData]("out").withPartitioner(RoundRobinPartitioner)

  override def shape(): StreamletShape = {
    StreamletShape.withOutlets(outTransactionData)
  }

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {

    override def runnableGraph(): RunnableGraph[_] = emitDataFromFiles.to(plainSink(outTransactionData))
  }

  private def emitDataFromFiles: Source[RawFileData, NotUsed] = {
    getFileList.flatMapConcat(path => readData(path))
  }

  private def readData(path: Path): Source[RawFileData, Future[IOResult]] = {
    FileIO.fromPath(path).via(Framing.delimiter(
      ByteString(delimiter), maxFrameLength, allowTruncation = true))
      .map(s => new RawFileData(s.utf8String))
  }

  private def getFileList: Source[Path, NotUsed] = {
    Directory.ls(FileSystems.getDefault.getPath(ConfigFactory.load("local")
      .getString(path)))
  }
}
