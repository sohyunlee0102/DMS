provider "aws" {
  region     = "ap-northeast-2"
}

resource "aws_dms_replication_task" "migration_task" {
  replication_task_id      = var.task_name
  migration_type           = var.migration_type
  replication_instance_arn = var.replication_instance_arn
  source_endpoint_arn      = var.source_endpoint_arn
  target_endpoint_arn      = var.target_endpoint_arn

  full_load_settings {
    target_table_prep_mode = var.target_table_preparation_mode
  }

  lob_settings {
    max_lob_size = var.max_lob_size
  }

  validation_settings {
    data_validation = var.data_validation
  }

  logging_settings {
    task_logs = var.task_logs
  }

  table_mappings = var.table_mappings
  tags = var.tags
}

variable "task_name" {}
variable "migration_type" {}
variable "target_table_preparation_mode" {}
variable "lob_column_settings" {}
variable "max_lob_size" {}
variable "data_validation" {}
variable "task_logs" {}
variable "stark_task_on_creation" {}
variable "source_endpoint_arn" {}
variable "target_endpoint_arn" {}
variable "replication_instance_arn" {}
variable "tags" {
  type = map(string)  # tags는 Map 형태로 받음
  default = {}        # 기본값은 빈 Map
}
variable "table_mappings" {
  description = "Table mappings"
  type        = string
  default     = "{}"  # 기본값을 빈 문자열로 설정
}

output "dms_task_arn" {
  value = aws_dms_replication_task.migration_task.replication_task_arn
}