provider "aws" {
  region = "ap-northeast-2"
}

resource "aws_dms_replication_task" "migration_task" {
  replication_task_id      = var.task_name
  migration_type           = var.migration_type
  replication_instance_arn = var.replication_instance_arn
  source_endpoint_arn      = var.source_endpoint_arn
  target_endpoint_arn      = var.target_endpoint_arn
  table_mappings          = var.table_mappings
  tags                     = var.tags

  replication_task_settings = jsonencode({
    FullLoadSettings = {
      TargetTablePrepMode = var.target_table_preparation_mode
    },
    LOBSettings = {
      MaxLobSize = var.max_lob_size
    },
    ValidationSettings = {
      DataValidation = var.data_validation
    },
    LoggingSettings = {
      TaskLogs = var.task_logs
    }
  })

}

variable "task_name" {
  description = "DMS Task name"
  type        = string
}

variable "migration_type" {
  description = "Type of migration"
  type        = string
}

variable "start_task_on_creation" {
  description = "Should the DMS task start on creation?"
  type        = bool
  default     = false
}

variable "target_table_preparation_mode" {
  description = "Target table preparation mode for full-load settings"
  type        = string
  default     = "DO_NOTHING"
}

variable "max_lob_size" {
  description = "LOB max size"
  type        = number
  default     = 32
}

variable "data_validation" {
  description = "Data validation setting"
  type        = bool
  default     = false
}

variable "task_logs" {
  description = "Should task logs be enabled?"
  type        = bool
  default     = false
}

variable "source_endpoint_arn" {
  description = "ARN for the source endpoint"
  type        = string
}

variable "target_endpoint_arn" {
  description = "ARN for the target endpoint"
  type        = string
}

variable "replication_instance_arn" {
  description = "ARN for the replication instance"
  type        = string
}

variable "table_mappings" {
  description = "Table Mappings JSON string"
  type        = string
}

variable "tags" {
  description = "Tags for the DMS Task"
  type        = map(string)
  default     = {}
}

output "dms_task_arn" {
  description = "The ARN of the created DMS replication task"
  value       = aws_dms_replication_task.migration_task.replication_task_arn
}