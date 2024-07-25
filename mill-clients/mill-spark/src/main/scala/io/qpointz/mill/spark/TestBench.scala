package io.qpointz.mill.spark

import org.apache.spark.sql.SparkSession

object TestBench {

    def main(args: Array[String]):Unit = {
        val spark = SparkSession.builder()
          .appName("localone")
          .master("local[*]")
          .getOrCreate()

        spark.read
          .format("io.qpointz.mill.spark")
          .option("lala","hala")
          .load("ns.table")


      print ("lala")
    }
}
