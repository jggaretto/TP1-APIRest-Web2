-- 1. Aseguramos que exista una lista por defecto para los favoritos huérfanos
INSERT INTO listas (nombre)
SELECT 'Sin clasificar'
WHERE NOT EXISTS (SELECT 1 FROM listas WHERE nombre = 'Sin clasificar');

-- 2. Reasignamos los favoritos que quedaron sin lista_id (creados antes del punto 5)
UPDATE favoritos
SET lista_id = (SELECT id FROM listas WHERE nombre = 'Sin clasificar')
WHERE lista_id IS NULL;

-- 3. Recién ahora declaramos la columna obligatoria
ALTER TABLE favoritos
    ALTER COLUMN lista_id SET NOT NULL;
