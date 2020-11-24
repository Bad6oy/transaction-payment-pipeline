package ru.neoflex.payment

import java.nio.file.{Path, Paths}
import akka.stream.IOResult
import akka.stream.alpakka.file.DirectoryChange
import akka.stream.alpakka.file.scaladsl.DirectoryChangesSource
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
  private val maxBufferSize: Int = 1000
  @transient private val path = "neoflex.file-ingress.volume-mounts.source-data"
  @transient private val outTransactionData: AvroOutlet[RawFileData] =
    AvroOutlet[RawFileData]("out").withPartitioner(RoundRobinPartitioner)

  @transient override def shape(): StreamletShape = {
    StreamletShape.withOutlets(outTransactionData)
  }

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {

    override def runnableGraph(): RunnableGraph[_] = emitDataFromFiles.to(plainSink(outTransactionData))
  }

  private def emitDataFromFiles = {
    val changes = DirectoryChangesSource(getPath, pollInterval = 3.second, maxBufferSize = maxBufferSize)
    changes.filter(directoryChanges).flatMapConcat(readData)
  }


  private def directoryChanges: ((Path, DirectoryChange)) => Boolean = { tuple2 =>
    tuple2._2 == DirectoryChange.Creation || tuple2._2 == DirectoryChange.Modification
  }

  private def readData: ((Path, DirectoryChange)) => Source[ByteString, Future[IOResult]]#Repr[ByteString]#Repr[RawFileData] = { tuple2 =>
    FileIO.fromPath(tuple2._1).via(Framing.delimiter(
      ByteString(System.lineSeparator()), maxFrameLength, allowTruncation = true))
      .map(s => new RawFileData(s.utf8String))
  }

  private def getPath: Path = {
    Paths.get(ConfigFactory.load("local").getString(path))
  }
}
