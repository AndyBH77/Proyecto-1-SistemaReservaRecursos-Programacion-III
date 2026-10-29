-- =====================================================================
-- Sistema de Reserva de Recursos - Proyecto #2 (EIF206 Programacion 3)
-- Script de la base de datos (MySQL)
--
-- Como ejecutarlo:
--   En SQL Developer, con la conexion "Reservas_proyecto", usar
--   "Ejecutar Script" (F5). Se puede ejecutar todas las veces que se
--   quiera: borra las tablas y las vuelve a crear con datos de prueba.
--
-- Nota: los datos de prueba van sin tildes a proposito, para evitar
-- problemas de codificacion al abrir el archivo en distintos programas.
-- =====================================================================

USE reservas_db;

-- ---------------------------------------------------------------------
-- 1. Borrar las tablas anteriores.
--    Se borran primero las que dependen de otras (las "hijas") y al
--    final las "padres"; si se hiciera al reves, MySQL no lo permitiria.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS reserva_recurso;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS recurso;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS funcionario;
DROP TABLE IF EXISTS usuario;

-- ---------------------------------------------------------------------
-- 2. USUARIO: todos los que pueden ingresar al sistema (login).
--    El rol indica si es administrador o funcionario.
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
                         id     VARCHAR(20)  NOT NULL,
                         clave  VARCHAR(100) NOT NULL,
                         rol    ENUM('ADMINISTRADOR', 'FUNCIONARIO') NOT NULL,
                         email  VARCHAR(100),
                         CONSTRAINT pk_usuario PRIMARY KEY (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 3. FUNCIONARIO: datos extra de los usuarios que son funcionarios.
--    Su id es el mismo que en USUARIO (relacion 1 a 1).
--    ON DELETE CASCADE: si se borra el usuario, se borra su fila aqui.
-- ---------------------------------------------------------------------
CREATE TABLE funcionario (
                             id        VARCHAR(20)  NOT NULL,
                             nombre    VARCHAR(100) NOT NULL,
                             telefono  VARCHAR(20),
                             CONSTRAINT pk_funcionario PRIMARY KEY (id),
                             CONSTRAINT fk_funcionario_usuario FOREIGN KEY (id)
                                 REFERENCES usuario (id)
                                 ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 4. CATEGORIA de recursos (ej: "Sala para 10 personas").
--    AUTO_INCREMENT: MySQL genera el id solo (1, 2, 3...).
--    UNIQUE: no puede haber dos categorias con la misma descripcion.
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
                           id           INT          NOT NULL AUTO_INCREMENT,
                           descripcion  VARCHAR(100) NOT NULL,
                           CONSTRAINT pk_categoria PRIMARY KEY (id),
                           CONSTRAINT uq_categoria_descripcion UNIQUE (descripcion)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 5. RECURSO: cada unidad fisica (ej: "Laptop #238715").
--    El id es el numero de activo. Pertenece a una categoria.
--    ON DELETE RESTRICT: no se puede borrar una categoria que tenga
--    recursos (MySQL da error y el programa debe avisar al usuario).
-- ---------------------------------------------------------------------
CREATE TABLE recurso (
                         id            VARCHAR(30)  NOT NULL,
                         id_categoria  INT          NOT NULL,
                         descripcion   VARCHAR(100) NOT NULL,
                         CONSTRAINT pk_recurso PRIMARY KEY (id),
                         CONSTRAINT fk_recurso_categoria FOREIGN KEY (id_categoria)
                             REFERENCES categoria (id)
                             ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 6. RESERVA: la actividad que un funcionario programa.
--    CHECK: la hora de inicio debe ser antes que la de fin.
--    ON DELETE RESTRICT: no se puede borrar un funcionario con reservas.
-- ---------------------------------------------------------------------
CREATE TABLE reserva (
                         id              INT          NOT NULL AUTO_INCREMENT,
                         id_funcionario  VARCHAR(20)  NOT NULL,
                         actividad       VARCHAR(150) NOT NULL,
                         fecha           DATE         NOT NULL,
                         hora_inicio     TIME         NOT NULL,
                         hora_fin        TIME         NOT NULL,
                         estado          ENUM('ACTIVA', 'CANCELADA') NOT NULL DEFAULT 'ACTIVA',
                         CONSTRAINT pk_reserva PRIMARY KEY (id),
                         CONSTRAINT fk_reserva_funcionario FOREIGN KEY (id_funcionario)
                             REFERENCES funcionario (id)
                             ON DELETE RESTRICT ON UPDATE CASCADE,
                         CONSTRAINT ck_reserva_horas CHECK (hora_inicio < hora_fin)
) ENGINE = InnoDB;

-- Indice para que las busquedas por fecha (calendario, estadisticas)
-- sean rapidas.
CREATE INDEX idx_reserva_fecha ON reserva (fecha);

-- ---------------------------------------------------------------------
-- 7. RESERVA_RECURSO: que recursos quedaron asignados a cada reserva.
--    Es una tabla "intermedia": una reserva puede tener varios recursos
--    y un recurso puede estar en muchas reservas (en horarios distintos).
--    Si se borra la reserva, se borran sus filas aqui (CASCADE).
--    No se puede borrar un recurso que este en alguna reserva (RESTRICT).
-- ---------------------------------------------------------------------
CREATE TABLE reserva_recurso (
                                 id_reserva  INT         NOT NULL,
                                 id_recurso  VARCHAR(30) NOT NULL,
                                 CONSTRAINT pk_reserva_recurso PRIMARY KEY (id_reserva, id_recurso),
                                 CONSTRAINT fk_rr_reserva FOREIGN KEY (id_reserva)
                                     REFERENCES reserva (id)
                                     ON DELETE CASCADE,
                                 CONSTRAINT fk_rr_recurso FOREIGN KEY (id_recurso)
                                     REFERENCES recurso (id)
                                     ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================

-- Usuarios: 1 administrador y 4 funcionarios.
-- Segun el enunciado, la clave inicial de un funcionario es igual a su id.
INSERT INTO usuario (id, clave, rol, email) VALUES
                                                ('admin', 'admin1234', 'ADMINISTRADOR', 'admin@ejemplo.com'),
                                                ('111',   '111',       'FUNCIONARIO',   'juan.mora@ejemplo.com'),
                                                ('222',   '222',       'FUNCIONARIO',   'maria.mata@ejemplo.com'),
                                                ('333',   '333',       'FUNCIONARIO',   'pedro.arias@ejemplo.com'),
                                                ('444',   '444',       'FUNCIONARIO',   'ana.lopez@ejemplo.com');

INSERT INTO funcionario (id, nombre, telefono) VALUES
                                                   ('111', 'Juan Mora',   '8888-1111'),
                                                   ('222', 'Maria Mata',  '8888-2222'),
                                                   ('333', 'Pedro Arias', '8888-3333'),
                                                   ('444', 'Ana Lopez',   '8888-4444');

-- Categorias: no se escribe el id, MySQL lo genera (quedan 1 a 5).
INSERT INTO categoria (descripcion) VALUES
                                        ('Sala para 10 personas'),   -- id 1
                                        ('Laptop windows 11'),       -- id 2
                                        ('Sala de Juntas'),          -- id 3
                                        ('Proyector'),               -- id 4
                                        ('Microfono inalambrico');   -- id 5

INSERT INTO recurso (id, id_categoria, descripcion) VALUES
                                                        ('34343',   1, 'Sala 1 primer piso'),
                                                        ('34344',   1, 'Sala 2 segundo piso'),
                                                        ('238715',  2, 'Laptop #238715'),
                                                        ('45238',   2, 'Laptop #45238'),
                                                        ('452784',  3, 'Sala de Juntas - Torre A'),
                                                        ('PRY-001', 4, 'Proyector Epson #1'),
                                                        ('MIC-001', 5, 'Microfono inalambrico #1');

-- Reservas: algunas pasadas, algunas futuras y una cancelada,
-- para poder probar calendario, actividades y estadisticas.
INSERT INTO reserva (id_funcionario, actividad, fecha, hora_inicio, hora_fin, estado) VALUES
                                                                                          ('222', 'Revision de presupuesto', '2026-10-20', '10:00', '11:30', 'ACTIVA'),    -- id 1
                                                                                          ('111', 'Planificacion semanal',   '2026-10-21', '08:00', '09:00', 'CANCELADA'), -- id 2
                                                                                          ('222', 'Reunion clientes',        '2026-10-30', '08:00', '10:00', 'ACTIVA'),    -- id 3
                                                                                          ('111', 'Capacitacion',            '2026-11-02', '13:00', '16:00', 'ACTIVA'),    -- id 4
                                                                                          ('333', 'Cafe de cumpleanos',      '2026-11-03', '15:00', '16:00', 'ACTIVA'),    -- id 5
                                                                                          ('444', 'Coordinacion viaje',      '2026-11-04', '09:00', '11:00', 'ACTIVA');    -- id 6

-- Recursos asignados a cada reserva
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
                                                         (1, '34343'),
                                                         (2, '452784'),
                                                         (3, '34343'),
                                                         (3, '238715'),
                                                         (4, '452784'),
                                                         (4, 'PRY-001'),
                                                         (5, '34344'),
                                                         (6, '45238'),
                                                         (6, 'MIC-001');