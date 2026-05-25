variable "aws_region" {
  description = "AWS region where Lightsail resources will be created"
  type        = string
  default     = "us-east-1"
}

variable "bundle_id" {
  description = "Lightsail instance bundle (size). Run: aws lightsail get-bundles --region us-east-1 | jq '.bundles[] | {bundleId, price, ramSizeInGb}' to list options"
  type        = string
  default     = "micro_3_0"  # ~$5/month: 1GB RAM, 1 vCPU, 40GB SSD
}

variable "public_key_path" {
  description = "Path to your SSH public key file"
  type        = string
  default     = "~/.ssh/id_ed25519.pub"
}
