locals {
  app_name_slug = substr(replace(var.app_name,"/\\W|_|\\s/",""),0,20)
}