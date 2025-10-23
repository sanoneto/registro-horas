# config.hcl
listener "tcp" {
  address     = "0.0.0.0:8202"
  tls_disable = "true"  # Só para desenvolvimento; não usar em produção
}

storage "file" {
  path = "/vault/data"
}

ui = true