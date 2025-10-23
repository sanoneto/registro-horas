
-- Habilita função gen_random_uuid() (pgcrypto). Ajuste se preferir uuid-ossp.


CREATE TABLE IF NOT EXISTS public.jwt_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(2000) NOT NULL,
    username VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
     revoked BOOLEAN NOT NULL DEFAULT FALSE,
     public_id UUID NOT NULL,
    CONSTRAINT uq_jwt_token_public_id UNIQUE (public_id)
    );

-- Índice para consultas por username (útil para findTopByUsernameOrderByIssuedAtDesc)
CREATE INDEX IF NOT EXISTS idx_jwt_token_username_issued_at ON public.jwt_token (username, issued_at DESC);

-- Índice para busca por token
CREATE INDEX IF NOT EXISTS idx_jwt_token_token ON public.jwt_token (token);