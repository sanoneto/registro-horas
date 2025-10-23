# config.hcl
listener "tcp" {
  address     = "127.0.0.1:8202"
  tls_disable = "true"  # Só para desenvolvimento; não usar em produção
}

storage "file" {
  path = "./vault/data"
}

ui = true