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

import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, HeadBucketRequest, HeadObjectRequest, ListBucketsRequest, ListObjectsRequest, PutObjectRequest}

import java.io.File
import java.net.URI
import java.nio.file.{FileVisitOption, Files, Path, Paths}
import collection.JavaConverters._

object TestC {


  def main(args:Array[String]):Unit = {

    val creds = AwsBasicCredentials.create("AKIAIOSFODNN7EXAMPLE","wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")

    val s3 = S3Client.builder()
      .region(Region.US_WEST_1)
      .endpointOverride(URI.create("http://localhost:9000"))
      .credentialsProvider(StaticCredentialsProvider.create(creds))
      .build()

    val lbreq = ListBucketsRequest.builder().build()
    val lbres = s3.listBuckets(lbreq).buckets().asScala
    lbres.foreach(f=> println(f.name()))

    val objreq = ListObjectsRequest.builder()
      .bucket("buck2").build()
    val objs = s3.listObjects(objreq)


    objs.contents().asScala.foreach(f=> println(f))

    def uploadFile(rootPath:Path, path: Path): Unit = {
        val rel = rootPath.toUri.relativize(path.toUri).getPath
        val por = PutObjectRequest.builder()
          .bucket("buck2")
          .key(s"auto/v1/${rel}")
          .metadata(Map("test"->"test").asJava)
          .build()
        s3.putObject(por, path)


        val pgr = HeadObjectRequest.builder()
          .bucket("buck2")
          .key(s"auto/v1/${rel}")
          .build()

        val mt = s3.headObject(pgr).metadata().asScala

        val hbr = HeadBucketRequest.builder()
          .bucket("buck2")
          .build()

        val rsp =  s3.headBucket(hbr)
        print(rsp)
    }

    val dir = Paths.get("/home/vm/wip/qpointz/qpointz/flow/test")
    Files.walk(dir).iterator().asScala.filter(_.toFile.isFile).foreach(x=>uploadFile(dir, x))

  }

}
