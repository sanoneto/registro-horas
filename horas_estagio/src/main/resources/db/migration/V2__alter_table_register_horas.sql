
-- 1) adicionar nova coluna com NOT NULL temporariamente NULL
ALTER TABLE public.register_horas ADD COLUMN valor_new double precision;


-- 3) aplicar NOT NULL e DEFAULT se desejado
ALTER TABLE public.register_horas ALTER COLUMN valor_new SET DEFAULT 0::double precision;
ALTER TABLE public.register_horas ALTER COLUMN valor_new SET NOT NULL;
