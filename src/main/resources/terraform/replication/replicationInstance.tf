provider "aws" {
  region     = "ap-northeast-2"
}

resource "aws_dms_replication_instance" "replication_instance" {
  replication_instance_class = var.instance_class
  replication_instance_id    = var.instance_name
  allocated_storage          = var.storage
  vpc_security_group_ids     = [var.vpc]
  tags = merge(var.RI_tags, {
    Description = var.description
  })
  publicly_accessible = var.public_accessible
  replication_subnet_group_id = var.subnet_group
  multi_az = var.high_availability != "" ? var.high_availability == "true" : false

}

variable "instance_class" {}
variable "instance_name" {}
variable "engine_version" {}
variable "high_availability" {}
variable "storage" {}
variable "vpc" {
  description = "VPC security group IDs"
  type        = string  # set of strings
}
variable "subnet_group" {}
variable "public_accessible" {}
variable "description" {}
variable "RI_tags" {
  type = map(string)  # tags는 Map 형태로 받음
  default = {}        # 기본값은 빈 Map
}

output "source_endpoint_arn" {
  value = aws_dms_replication_instance.replication_instance.replication_instance_arn
}