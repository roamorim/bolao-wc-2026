terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_lightsail_key_pair" "bolao" {
  name       = "bolao-key"
  public_key = file(pathexpand(var.public_key_path))
}

resource "aws_lightsail_instance" "bolao" {
  name              = "bolao-wc-2026"
  availability_zone = "${var.aws_region}a"
  blueprint_id      = "ubuntu_22_04"
  bundle_id         = var.bundle_id
  key_pair_name     = aws_lightsail_key_pair.bolao.name
  user_data         = file("${path.module}/user_data.sh")
}

resource "aws_lightsail_static_ip" "bolao" {
  name = "bolao-ip"
}

resource "aws_lightsail_static_ip_attachment" "bolao" {
  static_ip_name = aws_lightsail_static_ip.bolao.name
  instance_name  = aws_lightsail_instance.bolao.name
}

resource "aws_lightsail_instance_public_ports" "bolao" {
  instance_name = aws_lightsail_instance.bolao.name

  port_info {
    protocol  = "tcp"
    from_port = 22
    to_port   = 22
  }
  port_info {
    protocol  = "tcp"
    from_port = 80
    to_port   = 80
  }
  port_info {
    protocol  = "tcp"
    from_port = 443
    to_port   = 443
  }
}
