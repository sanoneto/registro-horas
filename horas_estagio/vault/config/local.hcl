storage "file" {
  path = "/vault/file"
}

listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = 1
}

ui = true

# Endereço público que o Vault anunciará (usado por clientes/peers)
api_addr = "http://127.0.0.1:8200"