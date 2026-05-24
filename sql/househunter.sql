-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 19-05-2026 a las 01:48:50
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `househunter`
--

-- --------------------------------------------------------
-- 1. TABLA SOBERANA: USUARIOS
-- --------------------------------------------------------
CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('ADMINISTRADOR','EMPRESA','INVITADO') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `usuarios` (`id`, `email`, `password`, `rol`) VALUES
(1, 'admin@hotel.com', '$2a$10$uF/66Rb9xmS8jD3cSVtP.eXIaRujgfBi9KD8e68y9Zd3NcgD6i83i', 'ADMINISTRADOR'),
(2, 'info@globant.com', '$2a$10$wjR9D2Q5HYWx41yfG89LE.PQDi.FF7WE7YNxHw4IMbxD9rH12MurC', 'EMPRESA'),
(3, 'invitado@gmail.com', '$2a$10$q/ABRKqFBznwHcrPci3st.rAjlfxQkZMzrAHGXmSi7zjPMFkUHCo6', 'INVITADO'),
(4, 'invitado1@gmail.com', '$2a$10$tHd/HAtSKXNyhggL5TYfYe5HcYWe1gA04cLpY0OJG2FIYxMz7u2bm', 'INVITADO'),
(6, 'Robert@gmail.com', '$2a$10$vQNuD3WE0qG5r.yeGE2Bvu1qMRJKvzS/aXuz0y0nNJOsRxwc5m94i', 'INVITADO');

-- --------------------------------------------------------
-- 2. TABLAS COMPLEMENTARIAS DE USUARIOS
-- --------------------------------------------------------
CREATE TABLE `datos_empresas` (
  `id_usuario` int(11) NOT NULL,
  `cuit` varchar(20) NOT NULL,
  `razon_social` varchar(100) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `cuit` (`cuit`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `datos_empresas` (`id_usuario`, `cuit`, `razon_social`) VALUES
(2, '30-12345678-9', 'Globant S.A.');

CREATE TABLE `datos_personas` (
  `id_usuario` int(11) NOT NULL,
  `dni` varchar(15) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) DEFAULT NULL,
  `celular` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `dni` (`dni`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `datos_personas` (`id_usuario`, `dni`, `nombre`, `apellido`, `celular`) VALUES
(1, '47299224', 'Augusto', 'Karacsany', NULL),
(3, '44555666', 'Luca', 'Borrelli', NULL),
(4, '44728397', 'Franco', 'Colapinto', NULL),
(6, '12345678', 'Robert', 'Trebor', NULL);

-- --------------------------------------------------------
-- 3. TABLAS DEL NEGOCIO HOTELERO / RESERVAS
-- --------------------------------------------------------
CREATE TABLE `habitaciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `numero` varchar(10) NOT NULL,
  `estado` enum('Libre','Half','Completa') DEFAULT 'Libre',
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero` (`numero`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `habitaciones` (`id`, `numero`, `estado`) VALUES
(1, '101', 'Libre'),
(2, '102', 'Libre'),
(3, '103', 'Libre'),
(4, '201', 'Libre'),
(5, '202', 'Libre'),
(6, '203', 'Libre');

CREATE TABLE `reservas_hotel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_empresa` int(11) NOT NULL,
  `codigo_unico_evento` varchar(50) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `cantidad_estimada_asistentes` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo_unico_evento` (`codigo_unico_evento`),
  KEY `id_empresa` (`id_empresa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- 4. TABLAS RELACIONADAS A RESERVAS Y OPERACIONES
-- --------------------------------------------------------
CREATE TABLE `actividades` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_reserva` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `importancia` enum('Alta','Media','Baja') NOT NULL,
  `categoria` enum('Cowork','Charlas','Recreacion','Descanso','Variados','Otros') NOT NULL,
  `hora_actividad` time NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_reserva` (`id_reserva`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `asignaciones_habitaciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_reserva` int(11) NOT NULL,
  `id_habitacion` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `casillero_checkin` enum('1','2') NOT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unica_asignacion_invitado` (`id_reserva`,`id_usuario`),
  KEY `id_habitacion` (`id_habitacion`),
  KEY `id_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `asistencias_actividades` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_actividad` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `asistio` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`id`),
  UNIQUE KEY `registro_unico_asistencia` (`id_actividad`,`id_usuario`),
  KEY `id_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `lista_invitados_previa` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_reserva` int(11) NOT NULL,
  `dni` varchar(15) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `celular` varchar(20) NOT NULL,
  `dni_companero` varchar(15) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unico_dni_reserva` (`id_reserva`,`dni`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `opiniones_feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_reserva` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `puntuacion_general` int(11) NOT NULL,
  `comentario` text DEFAULT NULL,
  `fecha_feedback` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `un_feedback_por_usuario` (`id_reserva`,`id_usuario`),
  KEY `id_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `premios` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_reserva` int(11) NOT NULL,
  `nombre_premio` varchar(150) NOT NULL,
  `entregado` char(1) DEFAULT 'N',
  `id_ganador_usuario` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_reserva` (`id_reserva`),
  KEY `id_ganador_usuario` (`id_ganador_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- 5. RESTRICCIONES Y LLAVES FORÁNEAS ( Los malditos CONSTRAINTS. )
-- --------------------------------------------------------
ALTER TABLE `datos_empresas`
  ADD CONSTRAINT `datos_empresas_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

ALTER TABLE `datos_personas`
  ADD CONSTRAINT `datos_personas_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

ALTER TABLE `reservas_hotel`
  ADD CONSTRAINT `reservas_hotel_ibfk_1` FOREIGN KEY (`id_empresa`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

ALTER TABLE `actividades`
  ADD CONSTRAINT `actividades_ibfk_1` FOREIGN KEY (`id_reserva`) REFERENCES `reservas_hotel` (`id`) ON DELETE CASCADE;

ALTER TABLE `asignaciones_habitaciones`
  ADD CONSTRAINT `asignaciones_habitaciones_ibfk_1` FOREIGN KEY (`id_reserva`) REFERENCES `reservas_hotel` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `asignaciones_habitaciones_ibfk_2` FOREIGN KEY (`id_habitacion`) REFERENCES `habitaciones` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `asignaciones_habitaciones_ibfk_3` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

ALTER TABLE `asistencias_actividades`
  ADD CONSTRAINT `asistencias_actividades_ibfk_1` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `asistencias_actividades_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

ALTER TABLE `lista_invitados_previa`
  ADD CONSTRAINT `lista_invitados_previa_ibfk_1` FOREIGN KEY (`id_reserva`) REFERENCES `reservas_hotel` (`id`) ON DELETE CASCADE;

ALTER TABLE `opiniones_feedback`
  ADD CONSTRAINT `opiniones_feedback_ibfk_1` FOREIGN KEY (`id_reserva`) REFERENCES `reservas_hotel` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `opiniones_feedback_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

ALTER TABLE `premios`
  ADD CONSTRAINT `premios_ibfk_1` FOREIGN KEY (`id_reserva`) REFERENCES `reservas_hotel` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `premios_ibfk_2` FOREIGN KEY (`id_ganador_usuario`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
