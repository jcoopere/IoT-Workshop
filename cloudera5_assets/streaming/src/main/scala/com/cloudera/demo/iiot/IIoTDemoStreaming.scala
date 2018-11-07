package com.cloudera.demo.iiot

import java.io.ByteArrayInputStream
import java.util.HashMap
import java.util.zip.GZIPInputStream

import scala.collection.JavaConverters._
import scala.util.Try

import com.trueaccord.scalapb.spark._

import kafka.serializer._
import org.apache.log4j.{Level, Logger}
import org.apache.kudu.client._
import org.apache.kudu.spark.kudu._
import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkContext, SparkConf}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{StreamingContext, Seconds}

import org.eclipse.kapua.service.device.call.message.kura.proto.kurapayload.KuraPayload

object IIoTDemoStreaming {

  case class Telemetry(id:String, millis:Option[Long], metric:String, value:Option[String])

  def main(args:Array[String]):Unit = {

    // Usage
    if (args.length == 0) {
      println("Args: <your_kafka_broker_1>:9092,<your_kafka_broker_2>:9092,<your_kafka_broker_3>:9092 <your_kudu_master_1>:7052 <kafka topic to read from> <kudu table to write to>")
      return
    }

    // Args
    val kafkaBrokerList = args(0)
    val kuduMasterList = args(1)
    val kafkaTopicIn = args(2)
    val kuduTelemetryTable = args(3)

    // Configure app
    val sparkConf = new SparkConf().setAppName("IIoTDemoStreaming")
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(3))
    val sqc = new SQLContext(sc)
    val kc = new KuduContext(kuduMasterList, sc)

    import sqc.implicits._

    // TODO: Use KafkaUtils.createDirectStream to consume raw data from Kafka
    //
    // Hint: Check out the example in for Creating a Direct Stream in Spark Streaming + Kafka Integration guide (https://spark.apache.org/docs/1.6.0/streaming-kafka-integration.html)
    //       Note that the example assumes Kafka message values are type String; but our Kura/ESF-generated protobuf is binary:
    //       Therefore, DefaultDecoder is the relevant class for value decoder, and we must specify Array[Byte] as the Kafka message value type.
    //
    // Note: Make sure you name the Kafka Direct Stream "kafkaDStream", because that's what the rest of this code expects.
    /* YOUR CODE HERE
    val kafkaParams = ...

    val kafkaDStream = ...
    */

    // Parse raw messages values into protobuf objects
    val kurapayloadDStream = kafkaDStream.map(message => {
      val key = message._1

      val value:KuraPayload = {
        val tryUnzipAndParse = Try { KuraPayload.parseFrom(new GZIPInputStream(new ByteArrayInputStream(message._2))) }
        if (tryUnzipAndParse.isSuccess) {
          tryUnzipAndParse.get
        }
        else {
          KuraPayload.parseFrom(message._2)
        }
      }

      (key, value)
    })

    kurapayloadDStream.print()

    // Convert (id, KuraPayload) tuple DStream into Telemetry DStream
    val telemetryDStream = kurapayloadDStream.flatMap(message => {
      val id = message._1
      val millis = message._2.timestamp

      val metricsList = message._2.metric

      var telemetryArray = new Array[Telemetry](metricsList.length)

      var i = 0
      for (metric <- metricsList) {
        val metricValue = {
          // Convert all metrics to String
          if (metric.`type`.isDouble) metric.doubleValue.map(_.toString)
          else if (metric.`type`.isFloat) metric.floatValue.map(_.toString)
          else if (metric.`type`.isInt64) metric.longValue.map(_.toString)
          else if (metric.`type`.isInt32) metric.intValue.map(_.toString)
          else if (metric.`type`.isBool) metric.boolValue.map(_.toString)
          else if (metric.`type`.isBytes) metric.bytesValue.map(_.toString)
          else metric.stringValue

        }
        telemetryArray(i) = new Telemetry(id, millis, metric.name, metricValue)
        i += 1
      }

      telemetryArray
    })

    telemetryDStream.print()

    // TODO: Convert each Telemetry RDD in the Telemetry DStream into a DataFrame and insert to Kudu
    //
    // Hint: Check out Kudu/Spark Integration documentation (https://kudu.apache.org/docs/developing.html#_kudu_integration_with_spark),
    //       as well as Spark's documentation for Output Operations on DStreams (https://spark.apache.org/docs/1.6.1/streaming-programming-guide.html#output-operations-on-dstreams).
    //       If we call foreachRDD on telemetryDStream, we can convert each RDD of "Telemetry" objects to a DataFrame implicitly using toDF(),
    //       then we can use our KuduContext to insert the rows of the DataFrame into our Kudu table.
    /* YOUR CODE HERE
    telemetryDStream.foreachRDD(...)
    */

    // Start app
    ssc.checkpoint("/tmp/checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
