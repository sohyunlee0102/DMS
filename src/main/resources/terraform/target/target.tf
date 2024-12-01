provider "aws" {
  region     = "ap-northeast-2"
}

# target endpoint 리소스
resource "aws_dms_endpoint" "target" {
  engine_name   = var.target_engine
  username      = var.target_username
  password      = var.target_password
  endpoint_id   = var.target_endpoint_id  # 변수를 사용하는 부분
  port          = var.target_port
  endpoint_type = "target"
  server_name = var.target_server_name
  tags = var.target_tags  # tags를 변수로 전달
}

variable "target_endpoint_id" {}
variable "target_username" {}
variable "target_password" {}
variable "target_server_name" {}
variable "target_port" {}
variable "target_engine" {}
variable "target_tags" {
  type = map(string)  # tags는 Map 형태로 받음
  default = {}        # 기본값은 빈 Map
}

output "target_endpoint_arn" {
  value = aws_dms_endpoint.target.endpoint_arn
}