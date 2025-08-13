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
 *  limitations under the License
 */

package io.qpointz.flow

import QId._

trait PackageQIds {
  protected val nsName:String

  lazy val ns = NamespaceId(nsName)

  def format(format:String, hierarchy: Seq[String]=Seq()): QGroupId = ns.hierarchyId(hierarchy:_*).groupId(format)
  def reader(fmt:String, hierarchy: Seq[String]=Seq()): QTypeId =  format(fmt, hierarchy).typeId("reader")
  def readerSettings(fmt:String, hierarchy: Seq[String]=Seq()): QTypeId =  format(fmt, hierarchy).typeId("reader-settings")
  def writer(fmt:String, hierarchy: Seq[String]=Seq()): QTypeId =  format(fmt, hierarchy).typeId("writer")
  def writerSettings(fmt:String, hierarchy: Seq[String]=Seq()): QTypeId =  format(fmt, hierarchy).typeId("writer-settings")
  def transformation(transformation:String, hierarchy: Seq[String]=Seq()): QTypeId=ns.hierarchyId(hierarchy:_*).groupId("transformation").typeId(transformation)
  def receipt(n:String, hierarchy: Seq[String]=Seq()): QTypeId =ns.hierarchyId(hierarchy:_*).groupId("receipt").typeId(n)
  def inputStream(n:String, hierarchy: Seq[String]=Seq()): QTypeId = ns.hierarchyId(hierarchy:_*).groupId("input-stream").typeId(n)
}
