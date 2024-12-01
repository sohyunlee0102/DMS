provider "aws" {
  region     = "ap-northeast-2"
}

# Source endpoint 리소스
resource "aws_dms_endpoint" "source" {
  engine_name   = var.source_engine
  username      = var.source_username
  password      = var.source_password
  endpoint_id   = var.source_endpoint_id  # 변수를 사용하는 부분
  port          = var.source_port
  endpoint_type = "source"
  server_name   = var.source_server_name

  # tags를 Map 형식으로 사용
  tags = var.source_tags  # tags를 변수로 전달
}

# 변수 정의
variable "source_endpoint_id" {}
variable "source_username" {}
variable "source_password" {}
variable "source_server_name" {}
variable "source_port" {}
variable "source_engine" {}

# tags 변수 정의
variable "source_tags" {
  type = map(string)  # tags는 Map 형태로 받음
  default = {}        # 기본값은 빈 Map
}

output "source_endpoint_arn" {
  value = aws_dms_endpoint.source.endpoint_arn
}