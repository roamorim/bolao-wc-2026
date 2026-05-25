output "instance_ip" {
  description = "Static public IP address of the Lightsail instance"
  value       = aws_lightsail_static_ip.bolao.ip_address
}

output "ssh_command" {
  description = "SSH command to connect to the instance"
  value       = "ssh ubuntu@${aws_lightsail_static_ip.bolao.ip_address}"
}

output "app_url" {
  description = "URL to access the application"
  value       = "http://${aws_lightsail_static_ip.bolao.ip_address}:8080"
}
