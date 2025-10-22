
-- 1) adicionar nova coluna com NOT NULL temporariamente NULL
ALTER TABLE public.register_horas ADD COLUMN valor_new double precision;

-- 2) popular com cast e tratar NULLs
UPDATE public.register_horas SET valor_new = COALESCE(horas_trabalhadas::double precision, 0);

-- 3) aplicar NOT NULL e DEFAULT se desejado
ALTER TABLE public.register_horas ALTER COLUMN valor_new SET DEFAULT 0::double precision;
ALTER TABLE public.register_horas ALTER COLUMN valor_new SET NOT NULL;

-- 4) dropar antiga e renomear
ALTER TABLE public.register_horas DROP COLUMN horas_trabalhadas;
ALTER TABLE public.register_horas RENAME COLUMN valor_new TO horas_trabalhadas;