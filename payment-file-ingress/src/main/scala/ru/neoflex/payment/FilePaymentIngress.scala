package ru.neoflex.payment

import java.nio.file.{FileSystems, Path, Paths}

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.alpakka.file.DirectoryChange
import akka.stream.alpakka.file.scaladsl.{Directory, DirectoryChangesSource}
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

  @transient private val pathFromConfig = ConfigFactory.load("local")
    .getString("neoflex.file-ingress.volume-mounts.source-data")

  @transient private val outTransactionData: AvroOutlet[RawFileData] =
    AvroOutlet[RawFileData]("out").withPartitioner(RoundRobinPartitioner)

  @transient override def shape(): StreamletShape = {
    StreamletShape.withOutlets(outTransactionData)
  }

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {

    override def runnableGraph(): RunnableGraph[_] = emitDataFromFiles.to(plainSink(outTransactionData))
  }

  private def emitDataFromFiles = {
    val readAtStartSource = getFileList.flatMapConcat(p => readData(p))
    val updatableSource = DirectoryChangesSource(getPath, pollInterval = 3.second, maxBufferSize = maxBufferSize)
      .filter(directoryChanges).flatMapConcat(tupleRead)
    readAtStartSource.concat(updatableSource)
  }

  private def directoryChanges: ((_, DirectoryChange)) => Boolean = { tuple2 =>
    tuple2._2 == DirectoryChange.Creation || tuple2._2 == DirectoryChange.Modification
  }

  private def tupleRead: ((Path, _)) => Source[ByteString, Future[IOResult]]#Repr[ByteString]#Repr[RawFileData] = { tuple2 =>
    readData(tuple2._1)
  }

  private def readData(path: Path): Source[RawFileData, Future[IOResult]] = {
    FileIO.fromPath(path).via(Framing.delimiter(
      ByteString(System.lineSeparator()), maxFrameLength, allowTruncation = true))
      .map(s => new RawFileData(s.utf8String))
  }

  private def getFileList: Source[Path, NotUsed] = {
    Directory.ls(FileSystems.getDefault.getPath(pathFromConfig))
  }

  private def getPath: Path = {
    Paths.get(pathFromConfig)
  }
}
