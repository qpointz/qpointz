locals {

  flow_sample_flat = flatten([
    for table, files in var.data : [
      for idx, file in files : {
        table = table
        file  = file
        name = "data/${table}/${idx}_${basename(file)}"
      }
    ]
  ])

}