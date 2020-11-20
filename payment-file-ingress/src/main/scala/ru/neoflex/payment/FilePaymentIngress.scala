package ru.neoflex.payment

import java.nio.file.{FileSystems, Path}

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl.{FileIO, Framing, RunnableGraph, Source}
import akka.util.ByteString
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.avro.AvroOutlet
import cloudflow.streamlets.{RoundRobinPartitioner, StreamletShape}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt


class FilePaymentIngress extends AkkaStreamlet {

  private val maxFrameLength: Int = 1024
  private val path = "neoflex.file-ingress.volume-mounts.source-data"
  @transient private val outTransactionData: AvroOutlet[RawFileData] =
    AvroOutlet[RawFileData]("out").withPartitioner(RoundRobinPartitioner)

  @transient override def shape(): StreamletShape = {
    StreamletShape.withOutlets(outTransactionData)
  }

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {

    override def runnableGraph(): RunnableGraph[_] = emitDataFromFiles.to(plainSink(outTransactionData))
  }

  private def emitDataFromFiles = {
    Source.tick(1.second, 5.second, NotUsed)
      .flatMapConcat(getFileList)
      .flatMapConcat(readData)
  }

  private def readData: Path => Source[RawFileData, Future[IOResult]] = { path: Path =>
    FileIO.fromPath(path).via(Framing.delimiter(
      ByteString(System.lineSeparator()), maxFrameLength, allowTruncation = true))
      .map(s => new RawFileData(s.utf8String))
  }

  private def getFileList: NotUsed => Source[Path, NotUsed] = { _ =>
    Directory.ls(FileSystems.getDefault.getPath(ConfigFactory.load("local")
      .getString(path)))
  }
}
