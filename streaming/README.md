## Streaming job
Spark Streaming job for parsing, denormalizing, and storing Kura protobuf metrics in Kudu.

## Sample Usage
### Build
`$ sbt clean assembly`
### Deploy
```
$ spark-submit \
  --master yarn \
  --deploy-mode client \
  --class com.cloudera.demo.iiot.IIoTDemoStreaming \
  target/scala-2.11/iiot-demo-assembly-1.0.jar \
  <your_kafka_broker_1>:9092,<your_kafka_broker_2>:9092,<your_kafka_broker_3>:9092 \
  <your_kudu_master>:7051 \
  <your_kafka_topic_name> \
  <your_kudu_table_name>
  ```
