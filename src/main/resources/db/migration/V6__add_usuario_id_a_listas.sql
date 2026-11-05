-- TP3: cada lista pasa a pertenecer a un usuario (Usuario -> Lista -> Favorito).
--
-- Las listas creadas durante el TP2 no tienen dueño. Para no perder datos, se
-- las asigna a un usuario "legacy" que no puede iniciar sesión: su hash es
-- inválido a propósito ('!'), así que ninguna contraseña puede coincidir.
-- Si la base no tiene listas, el usuario legacy no se crea.
INSERT INTO usuarios (email, password_hash, rol)
SELECT 'legacy@tp3.local', '!', 'USER'
WHERE EXISTS (SELECT 1 FROM listas)
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'legacy@tp3.local');

ALTER TABLE listas
    ADD COLUMN usuario_id BIGINT REFERENCES usuarios (id);

UPDATE listas
SET usuario_id = (SELECT id FROM usuarios WHERE email = 'legacy@tp3.local')
WHERE usuario_id IS NULL;

ALTER TABLE listas
    ALTER COLUMN usuario_id SET NOT NULL;

CREATE INDEX idx_listas_usuario_id ON listas (usuario_id);
