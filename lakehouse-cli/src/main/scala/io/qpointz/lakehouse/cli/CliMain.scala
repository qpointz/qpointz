/*
 * Copyright 2021 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.lakehouse.cli

import io.minio.{ListObjectsArgs, MakeBucketArgs, MinioClient, PutObjectArgs, UploadObjectArgs}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import java.nio.file.Paths
import scala.jdk.CollectionConverters._

object CliMain {

  def main(args:Array[String]): Unit = {

    println(Paths.get(".").toAbsolutePath.toString)

    val mcendpoint = "http://localhost:9000"
    val mcacckesskey = "AKIAIOSFODNN7EXAMPLE"
    val mcacckeesecret = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
    val mc = MinioClient.builder()
      .endpoint(mcendpoint)
      .credentials(mcacckesskey,mcacckeesecret)
      .build()


//    val end = new EndpointConfiguration("http://localhost:9000", "us-east-1")
//    val cred = new BasicAWSCredentials("AKIAIOSFODNN7EXAMPLE","wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
//    val amz = AmazonS3ClientBuilder
//      .standard()
//      .withEndpointConfiguration(end)
//      .withCredentials(new AWSStaticCredentialsProvider(cred))
//      .build();


    val mybucket = "mybucket"

        val bucketExists = mc.listBuckets().asScala.exists(f=> f.name().equals(mybucket))
        if (! bucketExists) {
          val arg = MakeBucketArgs.builder().bucket(mybucket).build()
          mc.makeBucket(arg)
        }

        val simpleExists = mc
          .listObjects(ListObjectsArgs.builder().bucket(mybucket).prefix("simple.csv").build())
          .asScala
          .nonEmpty
        if (!simpleExists) {
          mc.uploadObject(UploadObjectArgs.builder()
            .bucket(mybucket)
            .`object`("simple.csv")
            .filename("./test/data/simple.csv")
            .build())
        }


    val conf = new SparkConf(true)
      .setAppName("Spark minIO Test")
      .set("spark.hadoop.fs.s3a.endpoint", mcendpoint)
      .set("spark.hadoop.fs.s3a.access.key", mcacckesskey)
      .set("spark.hadoop.fs.s3a.secret.key", mcacckeesecret)
      .set("spark.hadoop.fs.s3a.path.style.access", "true")
      .set("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
      .set("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      .setMaster("local[*]")

    val session = SparkSession.builder().config(conf).getOrCreate()

    /*
    val data =  session.
        read.csv("s3a://" + mybucket + "/simple.csv").write.format("delta").save(s"s3a://${mybucket}/table3")
*/
    session.sql(s"CREATE TABLE simple USING DELTA LOCATION 's3a://${mybucket}/table3'")

    session.sql("SELECT * from simple").take(20).foreach(println)

    session.sql("SELECT * from simple").take(10).foreach(println)

//    val dt = DeltaTable.forPath(s"s3a://${mybucket}/table3")
//    dt.generate("symlink_format_manifest")

    //session.stop()
    //println(x)
  }

}
