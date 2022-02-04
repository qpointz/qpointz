/*
 * Copyright 2021 qpointz.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.qpointz.flow.aws.s3

import com.typesafe.config.ConfigFactory
import io.minio._
import _root_.org.scalatest.BeforeAndAfterAll
import _root_.org.scalatest.matchers.should.Matchers
import _root_.org.scalatest.flatspec.AnyFlatSpec

import java.io.FileOutputStream
import java.nio.file.{Files, Paths}
import java.util.{Random, UUID}
import scala.jdk.CollectionConverters._

class BaseTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  lazy val baseName = this.getClass.getName.replace(this.getClass.getPackageName, "")

  lazy val bucketName : String = {
    s"${baseName}${UUID.randomUUID().toString()}"
      .toLowerCase()
      .replace(".","")
      .replace("-","")
  }

  protected lazy val config = ConfigFactory.load()

  protected lazy val minioClient = MinioClient.builder()
    .endpoint(config.getString("io.qpointz.flow.aws.it.minio.url"))
    .credentials(
      config.getString("io.qpointz.flow.aws.it.minio.access_key_id"),
      config.getString("io.qpointz.flow.aws.it.minio.secret_access_key")
    )
    .build()


  override def beforeAll() : Unit = {
    val exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
    if (!exists) {
      println(s"make bucket ${bucketName}")
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
    }

    val rp = Files.createTempDirectory(bucketName)
    val rnd = new Random()

    def createFile(n:String, s:Int): Unit = {
      val bytes:Array[Byte]= new Array[Byte](s*1024)
      rnd.nextBytes(bytes)
      val f = Paths.get(rp.toString, n).toFile
      val fos = new FileOutputStream(f)
      fos.write(bytes)
      fos.flush()
      fos.close()
      println(f.toString.toString)

      minioClient.uploadObject(UploadObjectArgs.builder()
        .bucket(bucketName)
        .filename(f.toString)
        .`object`(s"/plain/$n")
        .build()
      )
    }

    (1 to 30).map(x=> (
        s"file${x}",
        1 + rnd.nextInt(2000)
      )
    )
    .foreach(x=>createFile(x._1,x._2))
  }

  override def afterAll(): Unit = {
    val exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
    if (exists) {
      println(s"remove objects in ${bucketName}")
      minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).recursive(true).build())
        .asScala.filter(!_.get().isDir).foreach(k=> {
        println(s"removing ${k.get().objectName()}")
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).`object`(k.get().objectName()).build())
      })
      println(s"remove bucket in ${bucketName}")
      minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build())
    }
  }

  behavior of "test"

  it should "match" in {
    assert(true)
  }

}
